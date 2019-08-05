package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class PluginIntegrationTest constructor(private val version: String) {
    @get:Rule val testProjectRoot = TemporaryFolder()

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun getParams(): List<String> {
            return listOf("5.0", "5.1", "5.2","5.3", "5.4", "5.5.1")
        }
    }

    @Test
    fun testSupportedVersion() {
        assumeSupportedVersion()
        writeBuildGradle(
                """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  disallowMultipleDaemons = false
                    |}
                """.trimMargin("|")
        )

        val result = runBuild()

        assertThat(result.output).contains("total dagger time was")
    }

    @Test
    fun testFailOnOlderVersion() {
        assumeUnsupportedVersion()
        writeBuildGradle(
                """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  disallowMultipleDaemons = false
                    |}
                """.trimMargin("|")
        )

        try {
            runBuild()
        } catch (e: UnexpectedBuildFailure) {
            assertThat(e).hasMessageThat().contains("Must be using Gradle Version 5.1 in order to use DoctorPlugin. Current Gradle Version is Gradle $version")
        }
    }

    @Test
    fun testFailWithMultipleDaemons() {
        assumeSupportedVersion()
        writeBuildGradle(
            """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  disallowMultipleDaemons = true
                    |}
                """.trimMargin("|")
        )
        try {
            runBuild()
        } catch (e: UnexpectedBuildFailure) {
            assertThat(e).hasMessageThat()
                .contains("This might be expected if you are working on multiple Gradle projects.")
        }
    }

    private fun runBuild(): BuildResult {
        return GradleRunner.create()
            .withProjectDir(testProjectRoot.root)
            .withPluginClasspath()
            .withGradleVersion(version)
            .build()
    }

    private fun assumeSupportedVersion() {
        Assume.assumeFalse("5.0" == version)
    }

    private fun assumeUnsupportedVersion() {
        Assume.assumeTrue(version == "5.0")
    }

    private fun writeBuildGradle(build: String) {
        writeFileToName("build.gradle", build)
    }

    private fun writeFileToName(fileName : String, contents: String) {
        testProjectRoot.newFile(fileName).writeText(contents)
    }
}