package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ConfigurationCacheTest {
    @get:Rule
    val testProjectRoot = TemporaryFolder()

    @Test
    fun configurationCacheWorks() {
        testProjectRoot.writeBuildGradle(
            """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  disallowMultipleDaemons = false
                    |  ensureJavaHomeMatches = false
                    |}
                """.trimMargin("|")
        )
        val fixtureName = "java-fixture"
        testProjectRoot.writeFileToName("settings.gradle", "include '$fixtureName'")
        testProjectRoot.setupFixture(fixtureName)

        val runner = GradleRunner.create()
            .forwardOutput()
            .withArguments("assemble", "--configuration-cache")
            .withProjectDir(testProjectRoot.root)
            .withGradleVersion("6.6")
            .withPluginClasspath()

        val result = runner.build()

        assertThat(result.output).contains("Configuration cache entry stored.")
        assertThat(result.output).contains("BUILD SUCCESSFUL")

        val resultTwo = runner.build()
        assertThat(resultTwo.output).contains("Reusing configuration cache.")
        assertThat(resultTwo.output).contains("BUILD SUCCESSFUL")
    }
}
