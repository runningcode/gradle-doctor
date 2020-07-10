package com.osacky.doctor

import com.osacky.doctor.DownloadSpeedMeasurer.ExternalDownloadEvent.Companion.fromGradleType
import com.osacky.doctor.internal.Finish
import com.osacky.doctor.internal.IntervalMeasurer
import com.osacky.doctor.internal.SlowNetworkPrinter
import com.osacky.doctor.internal.SlowNetworkPrinter.Companion.ONE_MEGABYTE
import io.reactivex.rxjava3.disposables.Disposable
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.resource.ExternalResourceReadBuildOperationType
import java.util.Collections

class DownloadSpeedMeasurer(
    private val buildOperations: BuildOperations,
    private val extension: DoctorExtension,
    private val intervalMeasurer: IntervalMeasurer
) : BuildStartFinishListener {

    private val slowNetworkPrinter = SlowNetworkPrinter("External Repos")
    private val downloadEvents = Collections.synchronizedList(mutableListOf<ExternalDownloadEvent>())
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

        synchronized(downloadEvents) {

            val totalBytes = downloadEvents.sumBy { event -> event.byteTotal.toInt() }
            val totalTime = intervalMeasurer.findTotalTime(downloadEvents.map { it.start to it.end })

            // Don't do anything if we didn't download anything.
            if (totalBytes == 0 || totalTime == 0L) {
                return Finish.None
            }
            val totalSpeed = (totalBytes / totalTime) / 1024f

            // Only print time if we downloaded at least one megabyte
            if (totalBytes > ONE_MEGABYTE) {
                if (totalSpeed < extension.downloadSpeedWarningThreshold) {
                    return Finish.FinishMessage(slowNetworkPrinter.obtainMessage(totalBytes, totalTime, totalSpeed))
                }
            }
        }
        return Finish.None
    }

    data class ExternalDownloadEvent(val start: Long, val end: Long, val byteTotal: Long) {
        companion object {
            fun fromGradleType(event: OperationFinishEvent): ExternalDownloadEvent {
                val result = event.result
                require(result is ExternalResourceReadBuildOperationType.Result)

                return ExternalDownloadEvent(event.startTime, event.endTime, result.bytesRead)
            }
        }
    }
}
