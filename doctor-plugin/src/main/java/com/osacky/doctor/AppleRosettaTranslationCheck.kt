package com.osacky.doctor

import com.osacky.doctor.AppleRosettaTranslationCheckMode.DISABLED
import com.osacky.doctor.AppleRosettaTranslationCheckMode.ERROR
import com.osacky.doctor.AppleRosettaTranslationCheckMode.WARN
import com.osacky.doctor.internal.CliCommandExecutor
import com.osacky.doctor.internal.PillBoxPrinter
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.nativeplatform.platform.OperatingSystem
import org.jetbrains.kotlin.com.google.common.annotations.VisibleForTesting

class AppleRosettaTranslationCheck(
    private val os: OperatingSystem,
    private val cliCommandExecutor: CliCommandExecutor,
    private val pillBoxPrinter: PillBoxPrinter,
    private val appleRosettaTranslationCheckMode: Property<AppleRosettaTranslationCheckMode>,
) : BuildStartFinishListener {
    @VisibleForTesting
    val isTranslatedCheckCommand = arrayOf("/bin/bash", "-c", "sysctl sysctl.proc_translated")

    @VisibleForTesting
    val translatedWithRosetta = "sysctl.proc_translated: 1"

    @VisibleForTesting
    val errorMessage =
        "Attempt to run Gradle under Apple Silicon Rosetta translation. Make sure Gradle uses JDK for aarch64 architecture."

    override fun onStart() {
        val appleRosettaTranslationCheckMode = appleRosettaTranslationCheckMode.get()
        if (appleRosettaTranslationCheckMode == DISABLED || !os.isMacOsX) return
        val output =
            runCatching {
                cliCommandExecutor.execute(isTranslatedCheckCommand, ignoreExitValue = true)
            }.getOrNull()
        if (output == translatedWithRosetta) {
            if (appleRosettaTranslationCheckMode == ERROR) {
                throw GradleException(pillBoxPrinter.createPill(errorMessage))
            } else if (appleRosettaTranslationCheckMode == WARN) {
                pillBoxPrinter.writePrescription(listOf(errorMessage))
            }
        }
    }

    override fun onFinish(): List<String> = emptyList()
}
