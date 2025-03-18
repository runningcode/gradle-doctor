package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.api.JavaVersion
import org.gradle.testkit.runner.internal.DefaultGradleRunner
import org.junit.Test

class ParallelGCIntegrationTest : AbstractIntegrationTest() {
    @Test
    fun testParallelGCWarningEnabled() {
        testProjectRoot.writeBuildGradle("")
        testProjectRoot.writeSettingsGradle(
            """
            plugins {
              id "com.osacky.doctor"
            }
            doctor {
              javaHome {
                ensureJavaHomeMatches = false
              }
            }
            """.trimIndent(),
        )

        val runner = createRunner()

        if (JavaVersion.current() <= JavaVersion.VERSION_1_8) {
            assertThat(runner.build().output).contains("SUCCESS")
        } else {
            assertThat(runner.buildAndFail().output).contains(
                """
              |> =============================== Gradle Doctor Prescriptions ============================================
              |  | For faster builds, use the parallel GC.                                                              |
              |  | Add -XX:+UseParallelGC to the org.gradle.jvmargs                                                     |
              |  ========================================================================================================
           """.trimMargin("|"),
            )
        }
    }

    @Test
    fun testParallelGCWarningWhenUsingParallelGC() {
        testProjectRoot.writeBuildGradle("")
        testProjectRoot.writeSettingsGradle(
            """
            plugins {
              id "com.osacky.doctor"
            }
            doctor {
              javaHome {
                ensureJavaHomeMatches = false
              }
            }
            """.trimIndent(),
        )

        val runner =
            (createRunner() as DefaultGradleRunner)
                .withJvmArguments("-XX:+UseParallelGC")

        assertThat(runner.build().output).contains("SUCCESS")
    }
}
