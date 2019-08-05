package com.osacky.doctor

import com.osacky.doctor.internal.DaemonCheck
import org.gradle.api.GradleException

class BuildDaemonChecker(private val extension: DoctorExtension, private val daemonCheck: DaemonCheck) : BuildStartFinishListener {
    override fun onStart() {
        if (extension.disallowMultipleDaemons) {
            val numberOfDaemons = daemonCheck.numberOfDaemons()
            if (numberOfDaemons > 1) {
                val message = """
                   $numberOfDaemons Gradle Daemons Active.
                   This might be expected if you are working on multiple Gradle projects.
                   Otherwise, there might be a settings mismatch between the IDE and the terminal.
                   There might also be a bug causing extra Daemons to spawn.
                """.trimIndent()
                throw GradleException(message)
            }
        }
    }

    override fun onFinish() {
    }
}