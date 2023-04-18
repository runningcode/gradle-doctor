package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.api.JavaVersion
import org.gradle.testkit.runner.internal.DefaultGradleRunner
import org.junit.Test

class OptimalGCIntegrationTest : AbstractIntegrationTest() {

    companion object {
        val PARALLEL_ERROR = """
              |> =============================== Gradle Doctor Prescriptions ============================================
              |  | For faster builds, use the parallel GC.                                                              |
              |  | Add -XX:+UseParallelGC to the org.gradle.jvmargs                                                     |
              |  ========================================================================================================
           """.trimMargin("|")

        val G1GC_ERROR = """
              |> =============================== Gradle Doctor Prescriptions ============================================
              |  | For faster builds, use the G1 GC.                                                                    |
              |  | Add -XX:+UseG1GC to the org.gradle.jvmargs                                                           |
              |  ========================================================================================================
           """.trimMargin("|")
    }


    @Test
    fun testParallelGCWarningEnabled() {
        testProjectRoot.writeBuildGradle(
            """
                    plugins {
                      id "com.osacky.doctor"
                    }
                    doctor {
                      javaHome {
                        ensureJavaHomeMatches = false
                      }
                    }
            """.trimIndent()
        )

        val runner = createRunner()

        JavaVersion.current().let {
            when {
                it <= JavaVersion.VERSION_1_8 -> {
                    assertThat(runner.build().output).contains("SUCCESS")
                }

                it in JavaVersion.VERSION_1_9..JavaVersion.VERSION_16 -> {
                    assertThat(runner.buildAndFail().output).contains(PARALLEL_ERROR)
                }

                it >= JavaVersion.VERSION_17 -> {
                    assertThat(runner.buildAndFail().output).contains(G1GC_ERROR)
                }
            }
        }
    }

    @Test
    fun testParallelGCWarningWhenUsingParallelGC() {
        testProjectRoot.writeBuildGradle(
            """
                    plugins {
                      id "com.osacky.doctor"
                    }
                    doctor {
                      javaHome {
                        ensureJavaHomeMatches = false
                      }
                    }
            """.trimIndent()
        )

        val runner = (createRunner() as DefaultGradleRunner)
            .withJvmArguments("-XX:+UseParallelGC")

        JavaVersion.current().let {
            when {
                it in JavaVersion.VERSION_1_9..JavaVersion.VERSION_16 -> {
                    assertThat(runner.build().output).contains("SUCCESS")
                }

                it >= JavaVersion.VERSION_17 -> {
                    assertThat(runner.buildAndFail().output).contains(G1GC_ERROR)
                }
            }
        }
    }

    @Test
    fun testG1GCWarningWhenUsingG1GC() {
        testProjectRoot.writeBuildGradle(
            """
                    plugins {
                      id "com.osacky.doctor"
                    }
                    doctor {
                      javaHome {
                        ensureJavaHomeMatches = false
                      }
                    }
            """.trimIndent()
        )

        val runner = (createRunner() as DefaultGradleRunner)
            .withJvmArguments("-XX:+UseG1GC")

        JavaVersion.current().let {
            when {
                it in JavaVersion.VERSION_1_9..JavaVersion.VERSION_16 -> {
                    assertThat(runner.buildAndFail().output).contains(PARALLEL_ERROR)
                }

                it >= JavaVersion.VERSION_17 -> {
                    assertThat(runner.build().output).contains("SUCCESS")
                }
            }
        }
    }
}
