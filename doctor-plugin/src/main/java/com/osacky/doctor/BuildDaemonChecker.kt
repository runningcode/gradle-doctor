package com.osacky.doctor

import com.osacky.doctor.internal.DaemonChecker
import com.osacky.doctor.internal.PillBoxPrinter
import org.gradle.api.GradleException

class BuildDaemonChecker(
    private val extension: DoctorExtension,
    private val daemonCheck: DaemonChecker,
    private val pillBoxPrinter: PillBoxPrinter
) : BuildStartFinishListener {

    override fun onStart() {
        if (extension.disallowMultipleDaemons.get()) {
            val errorMessage = daemonCheck.check()
            if (!errorMessage.isNullOrBlank()) {
                throw GradleException(pillBoxPrinter.createPill(errorMessage))
            }
        }
    }

    override fun onFinish(): List<String> = emptyList()
}
