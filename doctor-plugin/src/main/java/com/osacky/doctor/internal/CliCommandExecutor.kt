package com.osacky.doctor.internal

import org.gradle.api.Project

class CliCommandExecutor(private val project: Project) {

    @Suppress("UnstableApiUsage")
    fun execute(command: Array<String>): String {
        return project.providers.exec {
            commandLine(*command)
        }.standardOutput.asText.get().trim()
    }
}
