package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import com.osacky.doctor.internal.androidHome
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assume.assumeFalse
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class PluginIntegrationTest(
    private val version: String,
) {
    // AGP/Gradle compatibility matrix:
    // https://developer.android.com/build/releases/gradle-plugin#updating-gradle
    val agpVersion =
        when {
            version.startsWith("9.") -> "8.13.0"
            else -> "8.5.0"
        }

    @get:Rule val testProjectRoot = TemporaryFolder()

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun getParams(): List<String> = listOf("8.8", "9.5.1")
    }

    @Test
    fun testSupportedVersion() {
        testProjectRoot.writeSettingsGradle(
            """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  disallowMultipleDaemons = false
                    |  javaHome {
                    |    ensureJavaHomeMatches = false
                    |  }
                    |  warnWhenNotUsingParallelGC = false
                    |}
                """.trimMargin("|"),
        )

        val result = createRunner().build()

        assertThat(result.output).contains("BUILD SUCCESSFUL")
    }

    @Test
    fun testFailWithMultipleDaemons() {
        assumeNixLikeOs()
        testProjectRoot.writeSettingsGradle(
            """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  disallowMultipleDaemons = true
                    |  javaHome {
                    |    ensureJavaHomeMatches = !System.getenv().containsKey("CI")
                    |  }
                    |}
                """.trimMargin("|"),
        )
        val result = createRunner().buildAndFail()
        assertThat(result.output)
            .contains(
                """
                | This may indicate a settings mismatch between the IDE and the terminal.                              |
                | There might also be a bug causing extra Daemons to spawn.                                            |
                | You can check active Daemons with `jps`.                                                             |
                | To kill all active Daemons use:                                                                      |
                | pkill -f '.*GradleDaemon.*'                                                                          |
                |                                                                                                      |
                | This might be expected if you are working on multiple Gradle projects or if you are using build.grad |
                | le.kts.                                                                                              |
                | To disable this message add this to your root build.gradle file:                                     |
                | doctor {                                                                                             |
                |   disallowMultipleDaemons = false                                                                    |
                | }                                                                                                    |
                ========================================================================================================
                """.trimIndent(),
            )
    }

    @Test
    fun testJavaHomeNotSet() {
        testProjectRoot.writeSettingsGradle(
            """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  javaHome {
                    |    disallowMultipleDaemons = false
                    |    javaHome {
                    |      ensureJavaHomeMatches = true
                    |    }
                    |  }
                    |}
                """.trimMargin("|"),
        )

        val result =
            createRunner()
                .withEnvironment(mapOf("JAVA_HOME" to ""))
                .withArguments("tasks")
                .buildAndFail()
        assertThat(result.output.trimIndent()).contains(
            """
            =============================== Gradle Doctor Prescriptions ============================================
            | Gradle is not using JAVA_HOME.                                                                       |
            | JAVA_HOME is                                                                                         |
            """.trimIndent(),
        )
    }

    @Test
    fun testJavaHomeNotSetWithConsoleError() {
        testProjectRoot.writeSettingsGradle(
            """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  javaHome {
                    |    disallowMultipleDaemons = false
                    |    ensureJavaHomeMatches = true
                    |    failOnError = false
                    |  }
                    |}
                """.trimMargin("|"),
        )

        val result =
            createRunner()
                .withEnvironment(mapOf("JAVA_HOME" to ""))
                .withArguments("tasks")
                .build()
        // Still prints the error
        assertThat(result.output.trimIndent()).contains(
            """
            =============================== Gradle Doctor Prescriptions ============================================
            | Gradle is not using JAVA_HOME.                                                                       |
            | JAVA_HOME is                                                                                         |
            """.trimIndent(),
        )
    }

    @Test
    fun testJavaHomeNotSetWithCustomMessage() {
        testProjectRoot.writeSettingsGradle(
            """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  javaHome {
                    |    disallowMultipleDaemons = false
                    |    ensureJavaHomeMatches = true
                    |    extraMessage = "Check for more details here!"
                    |  }
                    |}
                """.trimMargin("|"),
        )

        val result =
            createRunner()
                .withEnvironment(mapOf("JAVA_HOME" to ""))
                .withArguments("tasks")
                .buildAndFail()
        assertThat(result.output).contains("Check for more details here!")
    }

    @Test
    fun testFailAssembleMultipleProjects() {
        testProjectRoot.newFile("local.properties").writeText("sdk.dir=${androidHome()}\n")
        testProjectRoot.writeBuildGradle(
            """
            buildscript {
              repositories {
                google()
                mavenCentral()
              }
              dependencies {
                classpath("com.android.tools.build:gradle:$agpVersion")
              }
            }
            """.trimIndent(),
        )
        testProjectRoot.writeSettingsGradle(
            """
            plugins {
              id "com.osacky.doctor"
            }
            doctor {
              disallowMultipleDaemons = false
              javaHome {
                ensureJavaHomeMatches = false
              }
              warnWhenNotUsingParallelGC = false
            }
            include 'app-one'
            include 'app-two'
            """.trimMargin(),
        )

        val srcFolder = testProjectRoot.newFolder("app-one", "src", "main")
        val folder = File(testProjectRoot.root, "app-one")
        createFileInFolder(srcFolder, "AndroidManifest.xml", "<manifest package=\"com.foo.bar.one\"/>")
        createFileInFolder(
            folder,
            "build.gradle",
            """
            apply plugin: 'com.android.application'

            android {
              namespace 'com.foo.bar.one'
              compileSdk 34
            }
            """.trimIndent(),
        )
        val srcFolder2 = testProjectRoot.newFolder("app-two", "src", "main")
        val folder2 = File(testProjectRoot.root, "app-two")
        createFileInFolder(srcFolder2, "AndroidManifest.xml", "<manifest package=\"com.foo.bar.two\"/>")
        createFileInFolder(
            folder2,
            "build.gradle",
            """
            apply plugin: 'com.android.application'

            android {
              namespace 'com.foo.bar.two'
              compileSdk 34
            }
            """.trimIndent(),
        )
        val result =
            createRunner()
                .withArguments("assembleDebug")
                .buildAndFail()
        assertThat(result.output).contains(
            """
               |=============================== Gradle Doctor Prescriptions ============================================
               || Did you really mean to run all these? [task ':app-one:assembleDebug', task ':app-two:assembleDebug'] |
               || Maybe you just meant to assemble/install one of them? In that case, you can try                      |
               ||   ./gradlew app-one:assembleDebug                                                                    |
               || Or did you hit "build" in the IDE (Green Hammer)? Did you know that assembles all the code in the en |
               || tire project?                                                                                        |
               || Next time try "Sync Project with Gradle Files" (Gradle Elephant with Arrow).                         |
               |========================================================================================================
            """.trimMargin(),
        )
    }

    @Test
    fun testFailInstallMultipleProjects() {
        testProjectRoot.newFile("local.properties").writeText("sdk.dir=${androidHome()}\n")
        testProjectRoot.writeBuildGradle(
            """
            buildscript {
              repositories {
                google()
                mavenCentral()
              }
              dependencies {
                classpath("com.android.tools.build:gradle:$agpVersion")
              }
            }
            """.trimIndent(),
        )
        testProjectRoot.writeSettingsGradle(
            """
            plugins {
              id "com.osacky.doctor"
            }
            doctor {
              disallowMultipleDaemons = false
              javaHome {
                ensureJavaHomeMatches = false
              }
              warnWhenNotUsingParallelGC = false
            }
            include 'app-one'
            include 'app-two'
            """.trimMargin(),
        )

        val srcFolder = testProjectRoot.newFolder("app-one", "src", "main")
        val folder = File(testProjectRoot.root, "app-one")
        createFileInFolder(srcFolder, "AndroidManifest.xml", "<manifest package=\"com.foo.bar.one\"/>")
        createFileInFolder(
            folder,
            "build.gradle",
            """
            apply plugin: 'com.android.application'

            android {
              namespace 'com.foo.bar.one'
              compileSdk 34
            }
            """.trimIndent(),
        )
        val srcFolder2 = testProjectRoot.newFolder("app-two", "src", "main")
        val folder2 = File(testProjectRoot.root, "app-two")
        createFileInFolder(srcFolder2, "AndroidManifest.xml", "<manifest package=\"com.foo.bar.two\"/>")
        createFileInFolder(
            folder2,
            "build.gradle",
            """
            apply plugin: 'com.android.application'

            android {
              namespace 'com.foo.bar.two'
              compileSdk 34
            }
            """.trimIndent(),
        )
        val result =
            createRunner()
                .withArguments("installDebug")
                .buildAndFail()
        assertThat(result.output).contains(
            """
               |=============================== Gradle Doctor Prescriptions ============================================
               || Did you really mean to run all these? [task ':app-one:installDebug', task ':app-two:installDebug']   |
               || Maybe you just meant to assemble/install one of them? In that case, you can try                      |
               ||   ./gradlew app-one:installDebug                                                                     |
               || Or did you hit "build" in the IDE (Green Hammer)? Did you know that assembles all the code in the en |
               || tire project?                                                                                        |
               || Next time try "Sync Project with Gradle Files" (Gradle Elephant with Arrow).                         |
               |========================================================================================================
            """.trimMargin(),
        )
    }

    private fun createRunner(): GradleRunner =
        GradleRunner
            .create()
            .withProjectDir(testProjectRoot.root)
            .withPluginClasspath()
            .withGradleVersion(version)

    private fun assumeNixLikeOs() {
        assumeFalse(DefaultNativePlatform.getCurrentOperatingSystem().isWindows)
    }

    private fun createFileInFolder(
        folder: File,
        fileName: String,
        contents: String,
    ) {
        File(folder, fileName).writeText(contents)
    }
}
