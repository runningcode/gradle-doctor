package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ObsoleteIntegrationTest {
    @get:Rule
    val testProjectRoot = TemporaryFolder()

    @Test
    fun failsBuildGradleDeclaration7() {
        generateBuildGradle()

        val result =
            GradleRunner
                .create()
                .withProjectDir(testProjectRoot.root)
                .withPluginClasspath()
                .withGradleVersion("7.6.4")
                .buildAndFail()

        assertThat(result.output.trimIndent()).contains(
            """
            An exception occurred applying plugin request [id: 'com.osacky.doctor']
            > Failed to apply plugin 'com.osacky.doctor'.
               > class org.gradle.api.internal.project.DefaultProject_Decorated cannot be cast to class org.gradle.api.initialization.Settings (org.gradle.api.internal.project.DefaultProject_Decorated and org.gradle.api.initialization.Settings are in unnamed module of loader org.gradle.internal.classloader.VisitableURLClassLoader
            """.trimIndent(),
        )
    }

    @Test
    fun failsBuildGradleDeclaration8() {
        generateBuildGradle()

        val result =
            GradleRunner
                .create()
                .withProjectDir(testProjectRoot.root)
                .withPluginClasspath()
                .withGradleVersion("8.10")
                .buildAndFail()

        assertThat(result.output.trimIndent()).contains(
            """
            An exception occurred applying plugin request [id: 'com.osacky.doctor']
            > Failed to apply plugin 'com.osacky.doctor'.
               > The plugin must be applied in a settings script (or to the Settings object), but was applied in a build script (or to the Project object)
            """.trimIndent(),
        )
    }

    private fun generateBuildGradle() {
        testProjectRoot.writeBuildGradle(
            """
            plugins {
              id "com.osacky.doctor"
              id 'java-library'
            }
            doctor {
              disallowMultipleDaemons = false
              javaHome {
                ensureJavaHomeMatches = false
              }
              warnWhenNotUsingParallelGC = false
            }
            """.trimIndent(),
        )
    }
}
