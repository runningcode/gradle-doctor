package com.osacky.doctor

import com.osacky.doctor.DownloadSpeedMeasurer.ExternalDownloadEvent.Companion.fromGradleType
import io.reactivex.disposables.Disposable
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.resource.ExternalResourceReadBuildOperationType

class DownloadSpeedMeasurer(private val buildOperations: BuildOperations) : BuildStartFinishListener {


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

    override fun onFinish() {
        val totalBytes = downloadEvents.sumBy { it.byteTotal.toInt() }
        val totalTime = downloadEvents.sumBy { it.duration.toInt() }


        // Don't do anything if we didn't download anything.
        if (totalBytes == 0 || totalTime == 0) {
            return
        }
        val totalSpeed = (totalBytes / totalTime) / 1024f

        // Only print time if we downloaded at least one megabyte
        if (totalBytes > ONE_MEGABYTE) {
            if (totalSpeed < 1.0f) {
                println("Detected a slow download speed downloading from External Repos.")
            }
            println("Total downloaded: $totalBytes bytes")
            println("Total time $totalTime ms")
            // TODO Decimal formatting
            println("Total speed = $totalSpeed MB/s")
        }
        disposable.dispose()
    }

    data class ExternalDownloadEvent(val duration: Long, val byteTotal: Long) {
        companion object {
            fun fromGradleType(event: OperationFinishEvent): ExternalDownloadEvent {
                val result = event.result
                require(result is ExternalResourceReadBuildOperationType.Result)

                return ExternalDownloadEvent(event.endTime - event.startTime, result.bytesRead)
            }
        }
    }

    companion object {
        const val ONE_MEGABYTE = 1024 * 1024
    }
}