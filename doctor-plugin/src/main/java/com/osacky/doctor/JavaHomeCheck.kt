package com.osacky.doctor

import com.osacky.doctor.internal.Finish
import com.osacky.doctor.internal.PillBoxPrinter
import org.gradle.api.GradleException
import org.gradle.internal.jvm.Jvm

class JavaHomeCheck(
    private val extension: DoctorExtension,
    private val pillBoxPrinter: PillBoxPrinter
) : BuildStartFinishListener {
    override fun onStart() {
        if (extension.ensureJavaHomeIsSet && environmentJavaHome == null) {
            throw GradleException(pillBoxPrinter.createPill("JAVA_HOME must be set."))
        }
        if (extension.ensureJavaHomeMatches && !isGradleUsingJavaHome()) {
            throw GradleException(pillBoxPrinter.createPill("""
                |Gradle is not using JAVA_HOME.
                |JAVA_HOME is $environmentJavaHome
                |Gradle is using $gradleJavaHome
                |This can slow down your build significantly when switching from command line to the terminal.
                |To fix: Project Structure -> JDK Location.
                |Set this to your JAVA_HOME.
            """.trimMargin()))
        }
    }

    override fun onFinish(): Finish {
        return Finish.None
    }

    private val environmentJavaHome = System.getenv("JAVA_HOME")
    private val gradleJavaHome = Jvm.current().javaHome

    private fun isGradleUsingJavaHome(): Boolean {
        if (environmentJavaHome != null && gradleJavaHome.startsWith(environmentJavaHome)) {
            return true
        }
        return false
    }
}
