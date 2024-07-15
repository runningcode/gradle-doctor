package com.osacky.doctor.internal

import java.util.concurrent.TimeUnit

interface Clock {
    fun upTime(): Long

    fun upTimeMillis(): Long
}

class SystemClock : Clock {
    override fun upTime(): Long = System.nanoTime()

    override fun upTimeMillis(): Long = upTime().toMillis()

    private fun Long.toMillis(): Long = TimeUnit.NANOSECONDS.toMillis(this)
}
