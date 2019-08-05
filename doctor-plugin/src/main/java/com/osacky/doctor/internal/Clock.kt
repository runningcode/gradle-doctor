package com.osacky.doctor.internal

interface Clock {
    fun upTime(): Long
}

class SystemClock : Clock {
    override fun upTime(): Long {
        return System.nanoTime()
    }
}
