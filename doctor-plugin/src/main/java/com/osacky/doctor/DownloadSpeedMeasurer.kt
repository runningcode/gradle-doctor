package com.osacky.doctor

import com.osacky.doctor.DownloadSpeedMeasurer.ExternalDownloadEvent.Companion.fromGradleType
import com.osacky.doctor.internal.Finish
import com.osacky.doctor.internal.SlowNetworkPrinter
import com.osacky.doctor.internal.SlowNetworkPrinter.Companion.ONE_MEGABYTE
import io.reactivex.rxjava3.disposables.Disposable
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.resource.ExternalResourceReadBuildOperationType
import org.slf4j.LoggerFactory

class DownloadSpeedMeasurer(
    private val buildOperations: BuildOperations,
    private val extension: DoctorExtension
) : BuildStartFinishListener {

    private val slowNetworkPrinter = SlowNetworkPrinter("External Repos")
    private val downloadEvents = mutableListOf<ExternalDownloadEvent>()
    private lateinit var disposable: Disposable

    override fun onStart() {
        disposable = buildOperations.finishes()
                .filter { it.result is ExternalResourceReadBuildOperationType.Result }
                .map { fromGradleType(it) }
                .subscribe { event ->
                    downloadEvents.add(event)
                }
    }

    override fun onFinish(): Finish {
        // Dispose first before summing byte totals otherwise we get crazy NPEs?
        disposable.dispose()

        val totalBytes = downloadEvents.sumBy { it.byteTotal.toInt() }
        val totalTime = downloadEvents.sumBy { it.duration.toInt() }

        // Don't do anything if we didn't download anything.
        if (totalBytes == 0 || totalTime == 0) {
            return Finish.None
        }
        val totalSpeed = (totalBytes / totalTime) / 1024f

        // Only print time if we downloaded at least one megabyte
        if (totalBytes > ONE_MEGABYTE) {
            if (totalSpeed < extension.downloadSpeedWarningThreshold) {
                return Finish.FinishMessage(slowNetworkPrinter.obtainMessage(totalBytes, totalTime, totalSpeed))
            }
        }
        return Finish.None
    }

    data class ExternalDownloadEvent(val duration: Long, val byteTotal: Long) {
        companion object {
            private val logger = LoggerFactory.getLogger(ExternalDownloadEvent::class.java)
            fun fromGradleType(event: OperationFinishEvent): ExternalDownloadEvent {
                val result = event.result
                require(result is ExternalResourceReadBuildOperationType.Result)

                if (result.bytesRead == null) {
                    logger.info("Null bytes read for $result")
                }
                return ExternalDownloadEvent(event.endTime - event.startTime, result.bytesRead)
            }
        }
    }
}
