package com.osacky.doctor.internal

import org.gradle.api.provider.ProviderFactory

class CliCommandExecutor(
    private val providers: ProviderFactory,
) {
    fun execute(
        command: Array<String>,
        ignoreExitValue: Boolean = false,
    ): String =
        providers
            .exec {
                commandLine(*command)
                isIgnoreExitValue = ignoreExitValue
            }.standardOutput.asText
            .get()
            .trim()
}
