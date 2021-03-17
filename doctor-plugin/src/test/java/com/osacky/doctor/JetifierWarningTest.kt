package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class JetifierWarningTest {

    @get:Rule
    val testProjectRoot = TemporaryFolder()

    private val jetifierWarning = "Jetifier was enabled which means your builds are slower by 4-20%."

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

        assertThat(result.output).contains(
            """
                =============================== Gradle Doctor Prescriptions ============================================
                | Jetifier was enabled which means your builds are slower by 4-20%.                                    |
                | Here's an article to help you disable it:                                                            |
                | https://adambennett.dev/2020/08/disabling-jetifier/                                                  |
                |                                                                                                      |
                | To disable this warning, configure the Gradle Doctor extension:                                      |
                | doctor {                                                                                             |
                |   warnWhenJetifierEnabled.set(false)                                                                 |
                | }                                                                                                    |
                ========================================================================================================

                BUILD SUCCESSFUL
            """.trimIndent()
        )
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
