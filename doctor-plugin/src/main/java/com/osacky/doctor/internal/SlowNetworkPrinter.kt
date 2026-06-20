package com.osacky.doctor.internal

import java.text.NumberFormat

class SlowNetworkPrinter(
    private val type: String,
    private val numberFormat: NumberFormat = twoDigits,
) {
    fun obtainMessage(
        totalBytes: Int,
        totalTime: Long,
        totalSpeed: Float,
    ): String {
        val megabytesDownloaded = numberFormat.format(totalBytes * 1.0f / ONE_MEGABYTE)
        val secondsDownloading = numberFormat.format(totalTime * 1.0f / 1000)
        val totalSpeedFormatted = numberFormat.format(totalSpeed)
        return """
            Detected a slow download speed downloading from $type.
            $megabytesDownloaded MB downloaded in $secondsDownloading s
            Total speed from $type = $totalSpeedFormatted MB/s
            """.trimIndent()
    }

    companion object {
        const val ONE_MEGABYTE = 1024 * 1024
    }
}
