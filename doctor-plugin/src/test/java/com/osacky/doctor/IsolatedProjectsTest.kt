package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class IsolatedProjectsTest {
    @get:Rule
    val testProjectRoot = TemporaryFolder()

    /**
     * Running this test produces an isolated project warning report in the test project because the JVM toolchain is
     * set to 8 in /doctor-plugin/build.gradle.kts. Once that is updated to 17 then the warning will go away.
     */
    @Test
    fun isolatedProjectsWorks() {
        testProjectRoot.writeBuildGradle("")
        val fixtureName = "java-fixture"
        testProjectRoot.writeFileToName(
            "settings.gradle",
            """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  javaHome {
                    |    disallowMultipleDaemons = false
                    |    ensureJavaHomeMatches = false
                    |  }
                    |  warnWhenNotUsingParallelGC = false
                    |}
                    |include '$fixtureName'
                """.trimMargin("|")
        )
        testProjectRoot.setupFixture(fixtureName)

        val runner =
            GradleRunner
                .create()
                .forwardOutput()
                .withArguments("assemble", "--configuration-cache", "-Dorg.gradle.unsafe.isolated-projects=true")
                .withProjectDir(testProjectRoot.root)
                .withGradleVersion("8.13")
                .withPluginClasspath()

        val result = runner.build()

        assertThat(result.output).contains("Isolated projects is an incubating feature.")
        assertThat(result.output).contains("Configuration cache entry stored.")
        assertThat(result.output).contains("BUILD SUCCESSFUL")

        val resultTwo = runner.build()
        assertThat(resultTwo.output).contains("Isolated projects is an incubating feature.")
        assertThat(resultTwo.output).contains("Reusing configuration cache.")
        assertThat(resultTwo.output).contains("BUILD SUCCESSFUL")
    }
}