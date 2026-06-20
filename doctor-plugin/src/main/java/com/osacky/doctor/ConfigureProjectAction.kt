package com.osacky.doctor

import org.gradle.api.IsolatedAction
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.testing.Test

internal class ConfigureProjectAction(
    private val enableTestCaching: Property<Boolean>,
    private val collectorServiceName: String,
) : IsolatedAction<Project> {
    override fun execute(project: Project) {
        project.tasks.withType(Test::class.java).configureEach {
            if (!enableTestCaching.get()) {
                outputs.upToDateWhen { false }
            }
        }
        val projectPath = project.path
        project.plugins.withId("com.android.application") {
            val collector =
                project.gradle.sharedServices.registrations
                    .getByName(collectorServiceName)
                    .service
                    .get() as AppProjectCollectorService
            collector.addProjectPath(projectPath)
        }
    }
}
