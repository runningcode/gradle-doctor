package com.osacky.doctor.internal

import java.lang.management.ManagementFactory

class DirtyBeanCollector {
    fun collect(): Int = ManagementFactory.getGarbageCollectorMXBeans().sumBy { it.collectionTime.toInt() }
}
