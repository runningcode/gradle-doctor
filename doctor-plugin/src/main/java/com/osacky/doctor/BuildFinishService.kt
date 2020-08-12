package com.osacky.doctor

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.util.LinkedList

abstract class BuildFinishService : BuildService<BuildServiceParameters.None>, AutoCloseable {

    private val closeList = LinkedList<AutoCloseable>()

    fun closeMeWhenFinished(closeable: AutoCloseable) {
        closeList.push(closeable)
    }

    override fun close() {
        for (item in closeList) {
            item.close()
        }
        closeList.clear()
    }
}
