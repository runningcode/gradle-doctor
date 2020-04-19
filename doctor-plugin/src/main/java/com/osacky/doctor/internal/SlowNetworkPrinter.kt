package com.osacky.doctor.internal

class SlowNetworkPrinter(private val type: String) {

    fun obtainMessage(totalBytes: Int, totalTime: Long, totalSpeed: Float): String {
        val megabytesDownloaded = twoDigits.format(totalBytes * 1.0f / ONE_MEGABYTE)
        val secondsDownloading = twoDigits.format(totalTime * 1.0f / 1000)
        val totalSpeedFormatted = twoDigits.format(totalSpeed)
        return """
                    Detected a slow download speed downloading from $type.
                    $megabytesDownloaded MB downloaded in $secondsDownloading s
                    Total speed from cache = $totalSpeedFormatted MB/s
                """.trimIndent()
    }

    companion object {
        const val ONE_MEGABYTE = 1024 * 1024
    }
}
