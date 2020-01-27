package com.osacky.doctor

import com.osacky.doctor.DownloadSpeedMeasurer.ExternalDownloadEvent.Companion.fromGradleType
import com.osacky.doctor.internal.Finish
import com.osacky.doctor.internal.twoDigits
import io.reactivex.rxjava3.disposables.Disposable
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.resource.ExternalResourceReadBuildOperationType
import org.slf4j.LoggerFactory

class DownloadSpeedMeasurer(
    private val buildOperations: BuildOperations,
    private val extension: DoctorExtension
) : BuildStartFinishListener {

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
        val totalBytes = downloadEvents.sumBy { it.byteTotal.toInt() }
        val totalTime = downloadEvents.sumBy { it.duration.toInt() }
        disposable.dispose()

        // Don't do anything if we didn't download anything.
        if (totalBytes == 0 || totalTime == 0) {
            return Finish.None
        }
        val totalSpeed = (totalBytes / totalTime) / 1024f

        // Only print time if we downloaded at least one megabyte
        if (totalBytes > ONE_MEGABYTE) {
            if (totalSpeed < extension.downloadSpeedWarningThreshold) {
                val megabytesDownloaded = twoDigits.format(totalBytes * 1.0f / ONE_MEGABYTE)
                val secondsDownloading = twoDigits.format(totalTime * 1.0f / 1000)
                val totalSpeedFormatted = twoDigits.format(totalSpeed)
                val message = """
                    Detected a slow download speed downloading from External Repos.
                    $megabytesDownloaded bytes downloaded in $secondsDownloading s
                    Total speed from maven: $totalSpeedFormatted MB/s
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
                require(result is ExternalResourceReadBuildOperationType.Result)

                if (result.bytesRead == null) {
                    logger.info("Null bytes read for $result")
                }
                return ExternalDownloadEvent(event.endTime - event.startTime, result.bytesRead)
            }
        }
    }

    companion object {
        const val ONE_MEGABYTE = 1024 * 1024
    }
}
