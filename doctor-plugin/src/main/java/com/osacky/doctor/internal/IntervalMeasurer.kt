package com.osacky.doctor.internal

import kotlin.math.max

class IntervalMeasurer {
    /**
     * Find the total time elapsed based on the union of the time intervals.
     * This is based on the following Stackoverflow answer but converted to Kotlin:
     * https://codereview.stackexchange.com/questions/126906/finding-the-total-time-elapsed-in-the-union-of-time-intervals
     */
    fun findTotalTime(intervals: List<Pair<Long, Long>>): Long {
        if (intervals.isEmpty()) {
            return 0L
        }
        val sorted = intervals.sortedBy { it.first }
        var totalTime = 0L
        var currentEnd = intervals[0].first

        sorted.forEach { interval ->
            if (interval.second > currentEnd) {
                totalTime += interval.second - max(interval.first, currentEnd)
                currentEnd = interval.second
            }
        }

        return totalTime
    }
}
