package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KotlinDaemonFallbackIntegrationTest : AbstractIntegrationTest() {

  val kotlinVersion = "2.1.21"

    @Test
    fun testDisallowKotlinCompileDaemonFallback() {
        writeKotlinBuildGradle(true)
        writeSettingsFile()
        testProjectRoot.newFolder("src/main/java/foo")
        testProjectRoot.newFolder("src/test/java/foo")
        testProjectRoot.writeFileToName(
            "src/main/java/Foo.kt",
            """
            package foo
            class Foo {
                fun bar() {
                    println("Hello, world!")
                }
            }
            """.trimIndent(),
        )
        testProjectRoot.writeFileToName(
            "src/test/java/Foo.kt",
            """
            package foo
            class Foo {
                fun bar() {
                    println("Hello, world!")
                }
            }
            """.trimIndent(),
        )

        val result = assembleRunnerWithIncorrectDaemonArguments().build()

        assertThat(result.output).contains("Could not connect to kotlin daemon. Using fallback strategy.")
        assertThat(result.output).contains("The Kotlin Compiler Daemon failed to connect and likely won't recover on its own.")
    }

    @Test
    fun allowKotlinCompileFallback() {
        writeKotlinBuildGradle(false)
        writeSettingsFile()
        testProjectRoot.newFolder("src/main/java/foo")
        testProjectRoot.writeFileToName(
            "src/main/java/foo/Foo.kt",
            """
            package foo
            class Foo {
                fun bar() {
                    println("Hello, world!")
                }
            }
            """.trimIndent(),
        )

        val result = assembleRunnerWithIncorrectDaemonArguments().build()

        assertThat(result.output).contains("Could not connect to kotlin daemon. Using fallback strategy.")
        assertThat(result.output).contains("SUCCESS")
    }

    private fun writeSettingsFile() {
        testProjectRoot.writeFileToName(
            "settings.gradle",
            """
            pluginManagement {
              repositories {
                mavenCentral()
                gradlePluginPortal()
              }
            }
            """.trimIndent(),
        )
    }

    private fun assembleRunnerWithIncorrectDaemonArguments() =
        createRunner()
            .withArguments("check", "-Dkotlin.daemon.jvm.options=invalid_jvm_argument_to_fail_process_startup")

    private fun writeKotlinBuildGradle(allowDaemonFallback: Boolean) {
        testProjectRoot.writeBuildGradle(
            """
            plugins {
              id "com.osacky.doctor"
              id "org.jetbrains.kotlin.jvm" version $kotlinVersion"
            }
            repositories {
              mavenCentral()
            }
            doctor {
              warnIfKotlinCompileDaemonFallback = $allowDaemonFallback
              warnWhenNotUsingParallelGC = false
              javaHome {
                ensureJavaHomeMatches = false
              }
            }
            """.trimIndent(),
        )
    }
}
