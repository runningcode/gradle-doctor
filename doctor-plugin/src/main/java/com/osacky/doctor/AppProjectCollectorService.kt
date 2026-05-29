package com.osacky.doctor

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.util.concurrent.ConcurrentHashMap

abstract class AppProjectCollectorService : BuildService<BuildServiceParameters.None> {
    private val appProjectPaths: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun addProjectPath(path: String) {
        appProjectPaths.add(path)
    }

    fun getProjectPaths(): Set<String> = appProjectPaths.toSet()
}
