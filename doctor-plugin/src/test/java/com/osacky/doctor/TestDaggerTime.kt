package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
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
                      ensureJavaHomeMatches = !System.getenv().containsKey("CI")
                    }
        """.trimIndent())
        testProjectRoot.newFile("settings.gradle").writeText("""
            include '$fixtureName'
        """.trimIndent())

        testProjectRoot.setupFixture(fixtureName)

        val result = GradleRunner.create()
            .withProjectDir(testProjectRoot.root)
            .withPluginClasspath()
            .withGradleVersion("5.6")
            .withArguments("assemble")
            .build()

        assertThat(result.output).containsMatch("This build spent 0.\\d+ s in Dagger Annotation Processors. |\n" +
                "Switch to Dagger Reflect to save some time.")
    }
}
