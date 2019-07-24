package com.osacky.doctor

import java.lang.management.ManagementFactory

class DirtyBeanCollector {

    fun collect(): Int {
        return ManagementFactory.getGarbageCollectorMXBeans().sumBy { it.collectionTime.toInt() }
    }
}