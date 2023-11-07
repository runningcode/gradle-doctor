package com.osacky.doctor.internal

import org.gradle.api.Project
import org.gradle.util.GradleVersion
import java.io.InputStream

class CliCommandExecutor(private val project: Project) {
    fun execute(command: Array<String>): String {
        return if (GradleVersion.current() >= GradleVersion.version("7.5")) {
            executeWithConfigCacheSupport(command)
        } else {
            executeInLegacyWay(command)
        }
    }

    @Suppress("UnstableApiUsage")
    private fun executeWithConfigCacheSupport(command: Array<String>): String {
        return project.providers.exec {
            commandLine(*command)
        }.standardOutput.asText.get().trim()
    }

    private fun executeInLegacyWay(command: Array<String>): String {
        fun InputStream.readToString() =
            use {
                it.readBytes().toString(Charsets.UTF_8).trim()
            }

        val process = Runtime.getRuntime().exec(command)
        if (process.waitFor() != 0) {
            throw RuntimeException(process.errorStream.readToString())
        }
        return process.inputStream.readToString()
    }
}
