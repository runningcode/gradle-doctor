package com.osacky.doctor

import com.osacky.doctor.internal.Finish
import com.osacky.doctor.internal.PillBoxPrinter
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.internal.jvm.Jvm
import java.io.File
import java.util.Collections

class JavaHomeCheck(
    private val extension: DoctorExtension,
    private val pillBoxPrinter: PillBoxPrinter,
    private val logger: Logger
) : BuildStartFinishListener {

    private val recordedErrors = Collections.synchronizedSet(LinkedHashSet<String>())

    override fun onStart() {
        val extraMessage = extension.javaHomeHandler.extraMessage.get()
        val failOnError = extension.javaHomeHandler.failOnError.get()

        if (extension.javaHomeHandler.ensureJavaHomeIsSet.get() && environmentJavaHome == null) {
            val message = buildString {
                appendln("JAVA_HOME is not set.")
                appendln("Please set JAVA_HOME so that switching between Android Studio and the terminal does not trigger a full rebuild.")
                appendln("To set JAVA_HOME: (using bash)")
                appendln("echo \"export JAVA_HOME=${'$'}(/usr/libexec/java_home)\" >> ~/.bash_profile")
                appendln("or `~/.zshrc` if using zsh.")
                extraMessage?.let {
                    appendln()
                    appendln(extraMessage)
                }
            }
            val pill = pillBoxPrinter.createPill(message)
            if (failOnError) {
                throw GradleException(pill)
            } else {
                recordedErrors.add(pill)
            }
        }
        if (extension.javaHomeHandler.ensureJavaHomeMatches.get() && !isGradleUsingJavaHome()) {
            val message = buildString {
                appendln("Gradle is not using JAVA_HOME.")
                appendln("JAVA_HOME is ${environmentJavaHome.toFile().toPath().toAbsolutePath()}")
                appendln("Gradle is using ${gradleJavaHome.toPath().toAbsolutePath()}")
                appendln("This can slow down your build significantly when switching from Android Studio to the terminal.")
                appendln("To fix: Project Structure -> JDK Location.")
                appendln("Set this to your JAVA_HOME.")
                extraMessage?.let {
                    appendln()
                    appendln(extraMessage)
                }
            }
            val pill = pillBoxPrinter.createPill(message)
            if (failOnError) {
                throw GradleException(pill)
            } else {
                recordedErrors.add(pill)
            }
        }
    }

    override fun onFinish(): Finish {
        if (recordedErrors.isNotEmpty()) {
            logger.error(recordedErrors.joinToString("\n\n"))
        }
        return Finish.None
    }

    private val environmentJavaHome = System.getenv("JAVA_HOME")
    private val gradleJavaHome = Jvm.current().javaHome

    private fun isGradleUsingJavaHome(): Boolean {
        // Follow symlinks when checking that java home matches.
        if (environmentJavaHome != null && gradleJavaHome.toPath().toRealPath() == File(environmentJavaHome).toPath().toRealPath()) {
            return true
        }
        return false
    }

    private fun String.toFile() = File(this)
}
