package com.osacky.doctor

import java.util.concurrent.TimeUnit

class GarbagePrinter(val clock: Clock, val collector: DirtyBeanCollector) {

    val startGarbageTime = collector.collect()
    val startBuildTime = clock.upTime().toMillis()

    fun onFinish() {
        val endGarbageTime = collector.collect()
        val endBuildTime = clock.upTime().toMillis()

        val buildDuration = endBuildTime - startBuildTime
        val garbageDuration = endGarbageTime - startGarbageTime


        println("build took $buildDuration")
        println("garbage took $garbageDuration")



    }

    private fun Long.toMillis(): Long {
        return TimeUnit.NANOSECONDS.toMillis(this)
    }
}
