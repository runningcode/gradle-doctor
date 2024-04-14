package com.osacky.doctor

import com.gradle.develocity.agent.gradle.adapters.BuildScanAdapter
import com.osacky.doctor.internal.DefaultPrescriptionGenerator
import com.osacky.doctor.internal.JAVA_HOME_TAG
import com.osacky.doctor.internal.JavaHomeCheckPrescriptionsGenerator
import com.osacky.doctor.internal.PillBoxPrinter
import org.gradle.api.GradleException
import java.io.File
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.pathString

internal const val JAVA_HOME = "JAVA_HOME"
internal const val JAVA_EXECUTABLES_FOLDER = "bin"
internal const val JAVA_HOME_NOT_FOUND =
    "There is no existing filesystem structure for %s. Please specify proper path for $JAVA_HOME!"

class JavaHomeCheck(
    jvmVariables: JvmVariables,
    private val javaHomeHandler: JavaHomeHandler,
    private val pillBoxPrinter: PillBoxPrinter,
    private val prescriptionsGenerator: JavaHomeCheckPrescriptionsGenerator =
        DefaultPrescriptionGenerator { javaHomeHandler.extraMessage.orNull },
) : BuildStartFinishListener, HasBuildScanTag {
    private val gradleJavaExecutablePath by lazy { resolveExecutableJavaPath(jvmVariables.gradleJavaHome) }
    private val environmentJavaExecutablePath by lazy { resolveEnvironmentJavaHome(jvmVariables.environmentJavaHome) }
    private val recordedErrors = Collections.synchronizedSet(LinkedHashSet<String>())
    private val isGradleUsingJavaHome: Boolean
        get() = gradleJavaExecutablePath == environmentJavaExecutablePath

    override fun onStart() {
        ensureJavaHomeIsSet()
        ensureJavaHomeMatchesGradleHome()
    }

    override fun onFinish(): List<String> {
        return recordedErrors.toList()
    }

    override fun addCustomValues(buildScanApi: BuildScanAdapter) {
        buildScanApi.tag(JAVA_HOME_TAG)
    }

    private fun ensureJavaHomeIsSet() {
        if (javaHomeHandler.ensureJavaHomeIsSet.get() && environmentJavaExecutablePath == null) {
            failOrRecordMessage(prescriptionsGenerator.generateJavaHomeIsNotSetMessage())
        }
    }

    private fun ensureJavaHomeMatchesGradleHome() {
        if (javaHomeHandler.ensureJavaHomeMatches.get() && !isGradleUsingJavaHome) {
            failOrRecordMessage(
                prescriptionsGenerator.generateJavaHomeMismatchesGradleHome(
                    environmentJavaExecutablePath?.pathString,
                    gradleJavaExecutablePath.pathString,
                ),
            )
        }
    }

    private fun failOrRecordMessage(message: String) {
        if (javaHomeHandler.failOnError.get()) {
            throw GradleException(pillBoxPrinter.createPill(message))
        } else {
            recordedErrors.add(message)
        }
    }

    private fun resolveEnvironmentJavaHome(location: String?) =
        location?.let {
            resolveExecutableJavaPath(it) { path ->
                if (!path.exists()) {
                    throw GradleException(String.format(JAVA_HOME_NOT_FOUND, path))
                }
                return@resolveExecutableJavaPath path
            }
        }

    private fun resolveExecutableJavaPath(
        location: String,
        fallback: (Path) -> Path = { it },
    ): Path {
        val path = File(location).toPath()
        return try {
            // Follow symlinks when checking that java home matches.
            path.resolve(JAVA_EXECUTABLES_FOLDER).toRealPath()
        } catch (exc: NoSuchFileException) {
            // fallback to initial path
            return fallback(path)
        }
    }
}

data class JvmVariables(val environmentJavaHome: String?, val gradleJavaHome: String)
