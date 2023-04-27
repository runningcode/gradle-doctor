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
                    |  warnWhenNotUsingParallelGC = false
                    |  failWhenNotUsingOptimalGC = false
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
                    |  warnWhenNotUsingParallelGC = false
                    |  failWhenNotUsingOptimalGC = false
                    |}
                """.trimMargin("|")
        )
        val fixtureName = "java-fixture"
        testProjectRoot.newFile("settings.gradle").writeText("include '$fixtureName'")
        testProjectRoot.setupFixture(fixtureName)
        testProjectRoot.newFolder("java-fixture", "src", "main", "java", "com", "foo")

        val result = GradleRunner.create()
            .withProjectDir(testProjectRoot.root)
            .withGradleVersion("6.8")
            .withPluginClasspath()
            .withArguments("assemble")
            .build()

        assertThat(result.output).contains("SUCCESS")
    }

    @Test
    fun cleanDependencyFailsBuild() {
        projectWithCleanDependency(disallowCleanTaskDependencies = true)

        val result = GradleRunner.create()
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
        """.trimMargin("|")
        )
    }

    @Test
    fun cleanDependencyDisabledSucceeds() {
        projectWithCleanDependency(disallowCleanTaskDependencies = false)
        val result = GradleRunner.create()
            .withProjectDir(testProjectRoot.root)
            .withPluginClasspath()
            .withArguments("clean")
            .build()

        assertThat(result.output).contains("BUILD SUCCESSFUL")
    }

    @Test
    fun cleanDependency74Succeeds() {
        projectWithCleanDependency(disallowCleanTaskDependencies = true)
        val result = GradleRunner.create()
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
                  failWhenNotUsingOptimalGC = false
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
            """.trimIndent()
        )
    }
}
