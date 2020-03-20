package com.osacky.doctor.internal

import java.util.concurrent.TimeUnit

interface Clock {
    fun upTime(): Long
    fun upTimeMillis(): Long
}

class SystemClock : Clock {
    override fun upTime(): Long {
        return System.nanoTime()
    }

    override fun upTimeMillis(): Long {
        return upTime().toMillis()
    }

    private fun Long.toMillis(): Long {
        return TimeUnit.NANOSECONDS.toMillis(this)
    }
}
