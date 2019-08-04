package com.osacky.doctor

import com.osacky.doctor.BuildCacheConnectionMeasurer.ExternalDownloadEvent.Companion.fromGradleType
import io.reactivex.disposables.Disposable
import org.gradle.caching.internal.operations.BuildCacheRemoteLoadBuildOperationType
import org.gradle.internal.operations.OperationFinishEvent

class BuildCacheConnectionMeasurer(private val buildOperations: BuildOperations) : BuildStartFinishListener {

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

    override fun onFinish() {
        val totalBytes = downloadEvents.sumBy { it.byteTotal.toInt() }
        val totalTime = downloadEvents.sumBy { it.duration.toInt() }


        // Don't do anything if we didn't download anything.
        if (totalBytes == 0 || totalTime == 0) {
            return
        }
        val totalSpeed = (totalBytes / totalTime) / 1024f

        // Only print time if we downloaded at least one megabyte
        if (totalBytes > DownloadSpeedMeasurer.ONE_MEGABYTE) {
            if (totalSpeed < 1.0f) {
                println("Detected a slow download speed downloading from Build Cache.")
            }
            println("Total downloaded from cache: $totalBytes bytes")
            println("Total time from cache $totalTime ms")
            // TODO Decimal formatting
            println("Total speed from cache = $totalSpeed MB/s")
        }
        disposable.dispose()
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