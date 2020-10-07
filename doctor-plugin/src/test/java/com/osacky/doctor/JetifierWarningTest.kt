package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class JetifierWarningTest {

    @get:Rule
    val testProjectRoot = TemporaryFolder()

    val jetifierWarning = "Jetifier was enabled which means your builds are slower by 4-20%."

    @Test
    fun testJetifierEnabledShowsWarning() {
        testProjectRoot.writeBuildGradle(
            """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  disallowMultipleDaemons = false
                    |  javaHome {
                    |    ensureJavaHomeMatches = false
                    |  }
                    |  warnWhenNotUsingParallelGC = false
                    |}
                """.trimMargin("|")
        )

        val result = GradleRunner.create()
            .forwardOutput()
            .withArguments("help", "-Pandroid.enableJetifier=true")
            .withPluginClasspath()
            .withProjectDir(testProjectRoot.root)
            .build()

        assertThat(result.output).contains("BUILD SUCCESSFUL")
        assertThat(result.output).contains("Gradle Doctor Prescriptions")
        assertThat(result.output).contains(jetifierWarning)
    }

    @Test
    fun testJetifierDisabledShowNoWarning() {
        testProjectRoot.writeBuildGradle(
            """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  disallowMultipleDaemons = false
                    |  javaHome {
                    |    ensureJavaHomeMatches = false
                    |  }
                    |  warnWhenNotUsingParallelGC = false
                    |}
                """.trimMargin("|")
        )

        val result = GradleRunner.create()
            .forwardOutput()
            .withArguments("help", "-Pandroid.enableJetifier=false")
            .withPluginClasspath()
            .withProjectDir(testProjectRoot.root)
            .build()

        assertThat(result.output).contains("BUILD SUCCESSFUL")
        assertThat(result.output).doesNotContain(jetifierWarning)
        assertThat(result.output).doesNotContain("Gradle Doctor Prescriptions")
    }
}
