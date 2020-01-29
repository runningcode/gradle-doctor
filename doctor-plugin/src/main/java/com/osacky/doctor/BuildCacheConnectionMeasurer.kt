package com.osacky.doctor

import com.osacky.doctor.BuildCacheConnectionMeasurer.ExternalDownloadEvent.Companion.fromGradleType
import com.osacky.doctor.internal.Finish
import com.osacky.doctor.internal.SlowNetworkPrinter
import com.osacky.doctor.internal.SlowNetworkPrinter.Companion.ONE_MEGABYTE
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Collections
import org.gradle.caching.internal.operations.BuildCacheRemoteLoadBuildOperationType
import org.gradle.internal.operations.OperationFinishEvent
import org.slf4j.LoggerFactory

class BuildCacheConnectionMeasurer(private val buildOperations: BuildOperations, private val extension: DoctorExtension) : BuildStartFinishListener {

    private val slowNetworkPrinter = SlowNetworkPrinter("Build Cache")
    private val downloadEvents = Collections.synchronizedList(mutableListOf<ExternalDownloadEvent>())
    private lateinit var disposable: Disposable
    override fun onStart() {
        disposable = buildOperations.finishes()
                .filter { (it.result is BuildCacheRemoteLoadBuildOperationType.Result) && (it.result as BuildCacheRemoteLoadBuildOperationType.Result).isHit }
                .map {
                    fromGradleType(it)
                }
                .subscribe {
                    downloadEvents.add(it)
                }
    }

    override fun onFinish(): Finish {
        // Dispose first before summing byte totals otherwise we get crazy NPEs?
        disposable.dispose()

        synchronized(downloadEvents) {
            val totalBytes = requireNotNull(downloadEvents) { "downloadEvents list cannot be null" }
                .sumBy { event -> requireNotNull(requireNotNull(event) { "ExternalDownloadEvent cannot be null" }.byteTotal) { "byteTotal cannot be null" }.toInt() }
            val totalTime = downloadEvents.sumBy { event -> event.duration.toInt() }

            // Don't do anything if we didn't download anything.
            if (totalBytes == 0 || totalTime == 0) {
                return Finish.None
            }

            // Only print time if we downloaded at least one megabyte
            if (totalBytes > ONE_MEGABYTE) {
                val totalSpeed = (totalBytes / totalTime) / 1024f
                if (totalSpeed < extension.downloadSpeedWarningThreshold) {
                    return Finish.FinishMessage(slowNetworkPrinter.obtainMessage(totalBytes, totalTime, totalSpeed))
                }
            }
            return Finish.None
        }
    }

    data class ExternalDownloadEvent(val duration: Long, val byteTotal: Long) {
        companion object {
            private val logger = LoggerFactory.getLogger(ExternalDownloadEvent::class.java)
            fun fromGradleType(event: OperationFinishEvent): ExternalDownloadEvent {
                val result = event.result
                require(result is BuildCacheRemoteLoadBuildOperationType.Result)
                if (!result.isHit) {
                    logger.debug("Received non-hit from $result, total was ${result.archiveSize}")
                    // If the result was not a hit, archive size and duration are undetermined so we set them to 0.
                    return ExternalDownloadEvent(0, 0)
                }
                return ExternalDownloadEvent(event.endTime - event.startTime, requireNotNull(result.archiveSize) { "Archive size was not null for $result" })
            }
        }
    }
}
