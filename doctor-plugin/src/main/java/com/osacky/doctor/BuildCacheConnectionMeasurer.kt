package com.osacky.doctor

import com.osacky.doctor.BuildCacheConnectionMeasurer.ExternalDownloadEvent.Companion.fromGradleType
import com.osacky.doctor.internal.Finish
import com.osacky.doctor.internal.twoDigits
import io.reactivex.rxjava3.disposables.Disposable
import org.gradle.caching.internal.operations.BuildCacheRemoteLoadBuildOperationType
import org.gradle.internal.operations.OperationFinishEvent
import org.slf4j.LoggerFactory

class BuildCacheConnectionMeasurer(private val buildOperations: BuildOperations, private val extension: DoctorExtension) : BuildStartFinishListener {

    private val downloadEvents = mutableListOf<ExternalDownloadEvent>()
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
        val totalBytes = downloadEvents.sumBy { it.byteTotal.toInt() }
        val totalTime = downloadEvents.sumBy { it.duration.toInt() }

        disposable.dispose()
        // Don't do anything if we didn't download anything.
        if (totalBytes == 0 || totalTime == 0) {
            return Finish.None
        }
        val totalSpeed = (totalBytes / totalTime) / 1024f

        // Only print time if we downloaded at least one megabyte
        if (totalBytes > DownloadSpeedMeasurer.ONE_MEGABYTE) {
            val megabytesDownloaded = twoDigits.format(totalBytes * 1.0f / DownloadSpeedMeasurer.ONE_MEGABYTE)
            val secondsDownloading = twoDigits.format(totalTime * 1.0f / 1000)
            val totalSpeedFormatted = twoDigits.format(totalSpeed)
            if (totalSpeed < extension.downloadSpeedWarningThreshold) {
                val message = """
                    Detected a slow download speed downloading from Build Cache.
                    $megabytesDownloaded bytes downloaded in $secondsDownloading s
                    Total speed from cache = $totalSpeedFormatted MB/s
                """.trimIndent()
                return Finish.FinishMessage(message)
            }
        }
        return Finish.None
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
