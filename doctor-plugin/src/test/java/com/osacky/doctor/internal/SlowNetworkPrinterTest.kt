package com.osacky.doctor.internal

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SlowNetworkPrinterTest {

    private val slowNetworkPrinter = SlowNetworkPrinter("Fake Repository")

    @Test
    fun obtainZeroMessage() {
        val message = slowNetworkPrinter.obtainMessage(0, 0, 0.0f)
        assertThat(message).isEqualTo(
            """
             Detected a slow download speed downloading from Fake Repository.
             0 MB downloaded in 0 s
             Total speed from cache = 0 MB/s
            """.trimIndent()
        )
    }

    @Test
    fun obtainDecimalsMessage() {
        val message = slowNetworkPrinter.obtainMessage(100512, 50, 0.1f)
        assertThat(message).isEqualTo(
            """
             Detected a slow download speed downloading from Fake Repository.
             0.1 MB downloaded in 0.05 s
             Total speed from cache = 0.1 MB/s
            """.trimIndent()
        )
    }

    @Test
    fun obtainGreaterThanOneMessage() {
        val message = slowNetworkPrinter.obtainMessage(10000512, 1700, 1.01f)
        assertThat(message).isEqualTo(
            """
             Detected a slow download speed downloading from Fake Repository.
             9.54 MB downloaded in 1.7 s
             Total speed from cache = 1.01 MB/s
            """.trimIndent()
        )
    }
}
