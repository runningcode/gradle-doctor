package com.osacky.doctor.internal

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class IntervalMeasurerTest {

    val underTest = IntervalMeasurer()

    @Test
    fun testSingleInterval() {
        val totalTime = underTest.findTotalTime(listOf(0L to 1L))

        assertThat(totalTime).isEqualTo(1L)
    }

    @Test
    fun testThreeOverlappingIntervals() {
        val totalTime = underTest.findTotalTime(listOf((0L to 1L), (0L to 1L), (0L to 1L)))

        assertThat(totalTime).isEqualTo(1L)
    }

    @Test
    fun testOverlappingIntervals() {
        val totalTime = underTest.findTotalTime(listOf((0L to 1L), (0L to 1L), (0L to 4)))

        assertThat(totalTime).isEqualTo(4)
    }
    @Test
    fun testNonOverlappingIntervals() {
        val totalTime = underTest.findTotalTime(listOf((0L to 1L), (0L to 1L), (4L to 6L)))

        assertThat(totalTime).isEqualTo(3)
    }
}
