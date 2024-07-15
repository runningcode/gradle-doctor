package com.osacky.doctor.internal

import org.gradle.api.Project
import org.gradle.util.GradleVersion
import java.io.InputStream

class CliCommandExecutor(
    private val project: Project,
) {
    fun execute(
        command: Array<String>,
        ignoreExitValue: Boolean = false,
    ): String =
        if (GradleVersion.current() >= GradleVersion.version("7.5")) {
            executeWithConfigCacheSupport(command, ignoreExitValue)
        } else {
            executeInLegacyWay(command, ignoreExitValue)
        }

    @Suppress("UnstableApiUsage")
    private fun executeWithConfigCacheSupport(
        command: Array<String>,
        ignoreExitValue: Boolean = false,
    ): String =
        project.providers
            .exec {
                commandLine(*command)
                isIgnoreExitValue = ignoreExitValue
            }.standardOutput.asText
            .get()
            .trim()

    private fun executeInLegacyWay(
        command: Array<String>,
        ignoreExitValue: Boolean = false,
    ): String {
        fun InputStream.readToString() =
            use {
                it.readBytes().toString(Charsets.UTF_8).trim()
            }

        val process = Runtime.getRuntime().exec(command)
        val processExitValue = process.waitFor()

        return when {
            (processExitValue == 0) || ignoreExitValue -> process.inputStream.readToString()
            else -> throw RuntimeException(process.errorStream.readToString())
        }
    }
}
