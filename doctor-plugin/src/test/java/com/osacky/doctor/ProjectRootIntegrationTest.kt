package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProjectRootIntegrationTest {
    @get:Rule
    val testProjectRoot = TemporaryFolder()

    @Test
    fun mustBeInProjectRoot() {
        val fixtureName = "non-root-fixture"
        testProjectRoot.newFile("build.gradle").writeText("")
        testProjectRoot.newFile("settings.gradle").writeText("include '$fixtureName'")
        testProjectRoot.setupFixture(fixtureName)

        val result =
            GradleRunner
                .create()
                .withProjectDir(testProjectRoot.root)
                .withPluginClasspath()
                .withGradleVersion("6.1.1")
                .withArguments("assemble")
                .buildAndFail()

        assertThat(result.output).contains("Gradle Doctor must be applied in the project root.")
    }
}
