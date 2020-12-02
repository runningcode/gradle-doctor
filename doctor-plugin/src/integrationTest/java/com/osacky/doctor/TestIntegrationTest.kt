package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TestIntegrationTest {
    @get:Rule
    val testProjectRoot = TemporaryFolder()

    @Test
    fun testIgnoreOnEmptyDirectories() {
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
                    |  failOnEmptyDirectories = true
                    |}
                """.trimMargin("|")
        )
        val fixtureName = "java-fixture"
        testProjectRoot.newFile("settings.gradle").writeText("include '$fixtureName'")
        testProjectRoot.setupFixture(fixtureName)
        testProjectRoot.newFolder("java-fixture", "src", "main", "java", "com", "foo")

        val result = GradleRunner.create()
            .withProjectDir(testProjectRoot.root)
            .withGradleVersion("6.7.1")
            .withPluginClasspath()
            .withArguments("assemble")
            .buildAndFail()

        assertThat(result.output).contains("Empty src dir(s) found. This causes build cache misses. Run the following command to fix it.")
    }

    @Test
    fun testDirectoriesIgnoredIn6dot8() {
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
                    |  failOnEmptyDirectories = true
                    |}
                """.trimMargin("|")
        )
        val fixtureName = "java-fixture"
        testProjectRoot.newFile("settings.gradle").writeText("include '$fixtureName'")
        testProjectRoot.setupFixture(fixtureName)
        testProjectRoot.newFolder("java-fixture", "src", "main", "java", "com", "foo")

        val result = GradleRunner.create()
            .withProjectDir(testProjectRoot.root)
            .withGradleVersion("6.8-rc-1")
            .withPluginClasspath()
            .withArguments("assemble")
            .build()

        assertThat(result.output).contains("SUCCESS")
    }
}
