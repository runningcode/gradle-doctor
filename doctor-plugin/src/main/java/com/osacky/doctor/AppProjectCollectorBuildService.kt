package com.osacky.doctor

import org.gradle.api.Project
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class AppProjectCollectorBuildService : BuildService<BuildServiceParameters.None> {
    private val appProjects = mutableSetOf<Project>()

    fun addProject(project: Project) {
        appProjects.add(project)
    }

    fun getProjects(): Set<Project> {
        return appProjects
    }
}

fun Project.getAppProjectCollectorBuildService(): AppProjectCollectorBuildService {
    return project.gradle.sharedServices.registerIfAbsent(
        "appProjectCollector",
        AppProjectCollectorBuildService::class.java,
        {}
    ).get()
}