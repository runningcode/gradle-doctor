package com.osacky.doctor

import com.osacky.doctor.BuildCacheConnectionMeasurer.ExternalDownloadEvent.Companion.fromGradleType
import com.osacky.doctor.internal.Finish
import io.reactivex.disposables.Disposable
import org.gradle.caching.internal.operations.BuildCacheRemoteLoadBuildOperationType
import org.gradle.internal.operations.OperationFinishEvent

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
            if (totalSpeed < extension.downloadSpeedWarningThreshold) {
                val message = """
                    Detected a slow download speed downloading from Build Cache.
                    $totalBytes bytes downloaded in $totalTime ms
                    Total speed from cache = $totalSpeed MB/s
                """.trimIndent()
                return Finish.FinishMessage(message)
            }
        }
        return Finish.None
    }

    data class ExternalDownloadEvent(val duration: Long, val byteTotal: Long) {
        companion object {
            fun fromGradleType(event: OperationFinishEvent): ExternalDownloadEvent {
                val result = event.result
                require(result is BuildCacheRemoteLoadBuildOperationType.Result)

                return ExternalDownloadEvent(event.endTime - event.startTime, result.archiveSize)
            }
        }
    }
}
