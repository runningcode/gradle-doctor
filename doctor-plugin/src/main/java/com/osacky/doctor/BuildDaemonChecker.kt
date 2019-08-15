package com.osacky.doctor

import com.osacky.doctor.internal.DaemonCheck
import com.osacky.doctor.internal.Finish
import com.osacky.doctor.internal.PillBoxPrinter
import org.gradle.api.GradleException

class BuildDaemonChecker(private val extension: DoctorExtension, private val daemonCheck: DaemonCheck, private val pillBoxPrinter: PillBoxPrinter) : BuildStartFinishListener {
    override fun onStart() {
        if (extension.disallowMultipleDaemons) {
            val numberOfDaemons = daemonCheck.numberOfDaemons()
            if (numberOfDaemons > 1) {
                val message = """
                   $numberOfDaemons Gradle Daemons Active.
                   This may indicate a settings mismatch between the IDE and the terminal.
                   There might also be a bug causing extra Daemons to spawn.
                   You can check active Daemons with `jps`.
                   To kill all active Daemons use:
                   pkill -f '.*GradleDaemon.*'

                   This might be expected if you are working on multiple Gradle projects or if you are using build.gradle.kts.
                   To disable this message add this to your root build.gradle file:
                   doctor {
                     disallowMultipleDaemons = false
                   }
                """.trimIndent()
                throw GradleException(pillBoxPrinter.createPill(message))
            }
        }
    }

    override fun onFinish(): Finish = Finish.None
}
