package com.osacky.doctor

interface Clock {
    fun upTime() : Long
}

class SystemClock : Clock {
    override fun upTime(): Long {
        return System.nanoTime()
    }
}
