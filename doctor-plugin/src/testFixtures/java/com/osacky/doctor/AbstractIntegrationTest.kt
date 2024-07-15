package com.osacky.doctor

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder

abstract class AbstractIntegrationTest {
    @get:Rule
    val testProjectRoot = TemporaryFolder()

    fun createRunner(): GradleRunner =
        GradleRunner
            .create()
            .withProjectDir(testProjectRoot.root)
            .withPluginClasspath()
}
