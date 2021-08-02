package com.osacky.doctor

import com.osacky.doctor.internal.PillBoxPrinter
import com.osacky.scan.tag.ScanApi
import org.gradle.api.GradleException
import org.gradle.internal.jvm.Jvm
import java.io.File
import java.util.Collections

class JavaHomeCheck(
    private val extension: DoctorExtension,
    private val pillBoxPrinter: PillBoxPrinter
) : BuildStartFinishListener, HasBuildScanTag {

    private val environmentJavaHome: String? = System.getenv("JAVA_HOME")
    private val gradleJavaHome = Jvm.current().javaHome
    private val recordedErrors = Collections.synchronizedSet(LinkedHashSet<String>())

    override fun onStart() {
        val extraMessage = extension.javaHomeHandler.extraMessage.orNull
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
            if (failOnError) {
                throw GradleException(pillBoxPrinter.createPill(message))
            } else {
                recordedErrors.add(message)
            }
        }
        if (extension.javaHomeHandler.ensureJavaHomeMatches.get() && !isGradleUsingJavaHome()) {
            val message = buildString {
                appendln("Gradle is not using JAVA_HOME.")
                appendln("JAVA_HOME is ${environmentJavaHome?.toFile()?.toPath()?.toAbsolutePath()}")
                appendln("Gradle is using ${gradleJavaHome.toPath().toAbsolutePath()}")
                appendln("This can slow down your build significantly when switching from Android Studio to the terminal.")
                appendln("To fix: Project Structure -> JDK Location.")
                appendln("Set this to your JAVA_HOME.")
                extraMessage?.let {
                    appendln()
                    appendln(extraMessage)
                }
            }
            if (failOnError) {
                throw GradleException(pillBoxPrinter.createPill(message))
            } else {
                recordedErrors.add(message)
            }
        }
    }

    override fun onFinish(): List<String> {
        return recordedErrors.toList()
    }

    private fun isGradleUsingJavaHome(): Boolean {
        // Follow symlinks when checking that java home matches.
        if (environmentJavaHome != null && gradleJavaHome.toPath().toRealPath() == File(environmentJavaHome).toPath().toRealPath()) {
            return true
        }
        return false
    }

    private fun String.toFile() = File(this)

    override fun addCustomValues(buildScanApi: ScanApi) {
        buildScanApi.tag("doctor-java-home")
    }
}
