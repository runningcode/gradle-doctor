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
    fun cleanDependencyFailsBuild() {
        projectWithCleanDependency(disallowCleanTaskDependencies = true)

        val result =
            GradleRunner
                .create()
                .withProjectDir(testProjectRoot.root)
                .withPluginClasspath()
                .withArguments("clean")
                .withGradleVersion("7.3.3")
                .buildAndFail()

        assertThat(result.output).contains(
            """
       |   > =============================== Gradle Doctor Prescriptions ============================================
       |     | Adding dependencies to the clean task could cause unexpected build outcomes.                         |
       |     | Please remove the dependency from task ':clean' on the following tasks: [foo].                       |
       |     | See github.com/gradle/gradle/issues/2488 for more information.                                       |
       |     ========================================================================================================
        """.trimMargin("|"),
        )
    }

    @Test
    fun cleanDependencyDisabledSucceeds() {
        projectWithCleanDependency(disallowCleanTaskDependencies = false)
        val result =
            GradleRunner
                .create()
                .withProjectDir(testProjectRoot.root)
                .withPluginClasspath()
                .withArguments("clean")
                .build()

        assertThat(result.output).contains("BUILD SUCCESSFUL")
    }

    @Test
    fun cleanDependency74Succeeds() {
        projectWithCleanDependency(disallowCleanTaskDependencies = true)
        val result =
            GradleRunner
                .create()
                .withProjectDir(testProjectRoot.root)
                .withPluginClasspath()
                .withGradleVersion("7.4")
                .withArguments("clean")
                .build()

        assertThat(result.output).contains("BUILD SUCCESSFUL")
    }

    fun projectWithCleanDependency(disallowCleanTaskDependencies: Boolean) {
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
              disallowCleanTaskDependencies = $disallowCleanTaskDependencies
            }
            
            tasks.register('foo') {
              doFirst {
                println 'foo'
              }
            }
            tasks.withType(Delete).configureEach {
              println 'configuring delete'
              dependsOn 'foo'
            }
            """.trimIndent(),
        )
    }
}
