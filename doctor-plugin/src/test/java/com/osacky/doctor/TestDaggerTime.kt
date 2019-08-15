package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TestDaggerTime {
    @get:Rule
    val testProjectRoot = TemporaryFolder()

    @Test
    fun testDaggerJvm() {
        val fixtureName = "dagger-jvm"
        testProjectRoot.newFile("build.gradle").writeText("""
                    plugins {
                      id "com.osacky.doctor"
                    }
                    doctor {
                      disallowMultipleDaemons = false
                      daggerThreshold = 100
                    }
        """.trimIndent())
        testProjectRoot.newFile("settings.gradle").writeText("""
            include '$fixtureName'
        """.trimIndent())

        setupFixture(fixtureName)

        val result = GradleRunner.create()
            .withProjectDir(testProjectRoot.root)
            .withPluginClasspath()
            .withGradleVersion("5.5.1")
            .withArguments("assemble")
            .build()

        assertThat(result.output).containsMatch("================== Gradle Doctor Prescriptions ===============================\n" +
                "| This build spent 0.\\d+ s in Dagger Annotation Processors                   |\n" +
                "| Switch to Dagger Reflect to save some time.                                |\n" +
                "==============================================================================")
    }

    private fun setupFixture(fixtureName: String) {
        File("src/test/fixtures/$fixtureName").copyRecursively(testProjectRoot.newFile(fixtureName), true)
    }
}
