package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import com.osacky.doctor.internal.androidHome
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.Assume
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class PluginIntegrationTest constructor(private val version: String) {
    val agpVersion = "4.0.1"
    @get:Rule val testProjectRoot = TemporaryFolder()

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun getParams(): List<String> {
            // Keep 6.0 as minimum unsupported version and 6.1 as minimum supported version.
            // Keep this list to 5 as testing against too many versions causes OOMs.
            return listOf("6.0.1", "6.5.1", "7.0", "7.4", "7.5.1")
        }
    }

    @Test
    fun testSupportedVersion() {
        assumeSupportedVersion()
        writeBuildGradle(
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
                """.trimMargin("|")
        )

        val result = createRunner().build()

        assertThat(result.output).contains("BUILD SUCCESSFUL")
    }

    @Test
    fun testFailOnOlderVersion() {
        assumeUnsupportedVersion()
        writeBuildGradle(
            """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  disallowMultipleDaemons = false
                    |  javaHome {
                    |    ensureJavaHomeMatches = !System.getenv().containsKey("CI")
                    |  }
                    |}
                """.trimMargin("|")
        )

        val result = createRunner().buildAndFail()
        assertThat(result.output).contains("Must be using Gradle Version 6.1.1 in order to use DoctorPlugin. Current Gradle Version is Gradle $version")
    }

    @Test
    fun testFailWithMultipleDaemons() {
        assumeSupportedVersion()
        writeBuildGradle(
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
                """.trimMargin("|")
        )
        val result = createRunner().buildAndFail()
        assertThat(result.output)
            .contains(
                """
                    |  | This may indicate a settings mismatch between the IDE and the terminal.                              |
                    |  | There might also be a bug causing extra Daemons to spawn.                                            |
                    |  | You can check active Daemons with `jps`.                                                             |
                    |  | To kill all active Daemons use:                                                                      |
                    |  | pkill -f '.*GradleDaemon.*'                                                                          |
                    |  |                                                                                                      |
                    |  | This might be expected if you are working on multiple Gradle projects or if you are using build.grad |
                    |  | le.kts.                                                                                              |
                    |  | To disable this message add this to your root build.gradle file:                                     |
                    |  | doctor {                                                                                             |
                    |  |   disallowMultipleDaemons = false                                                                    |
                    |  | }                                                                                                    |
                    |  ========================================================================================================
                    """.trimMargin()
            )
    }

    // This is failing, perhaps because it is actually trying to use "foo" as JAVA_HOME.
    @Test @Ignore
    fun testJavaHomeNotSet() {
        assumeSupportedVersion()

        writeBuildGradle(
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
                """.trimMargin("|")
        )
        testProjectRoot.newFile("settings.gradle")

        val result = createRunner()
            .withEnvironment(mapOf("JAVA_HOME" to "foo"))
            .withArguments("tasks")
            .buildAndFail()
        assertThat(result.output).contains(
            """
                |> =============================== Gradle Doctor Prescriptions ============================================
                |  | Gradle is not using JAVA_HOME.                                                                       |
                |  | JAVA_HOME is foo                                                                                     |
                |  """
                .trimMargin("|")
        )
    }

    // This is failing, perhaps because it is actually trying to use "foo" as JAVA_HOME.
    @Test @Ignore
    fun testJavaHomeNotSetWithConsoleError() {
        assumeSupportedVersion()

        writeBuildGradle(
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
                """.trimMargin("|")
        )
        testProjectRoot.newFile("settings.gradle")

        val result = createRunner()
            .withEnvironment(mapOf("JAVA_HOME" to "foo"))
            .withArguments("tasks")
            .buildAndFail()
        // Still prints the error
        assertThat(result.output).contains(
            """
                |> =============================== Gradle Doctor Prescriptions ============================================
                |  | Gradle is not using JAVA_HOME.                                                                       |
                |  | JAVA_HOME is foo                                                                                     |
                |  """
                .trimMargin("|")
        )
    }

    // This is failing, perhaps because it is actually trying to use "foo" as JAVA_HOME.
    @Test @Ignore
    fun testJavaHomeNotSetWithCustomMessage() {
        assumeSupportedVersion()

        writeBuildGradle(
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
                """.trimMargin("|")
        )
        testProjectRoot.newFile("settings.gradle")

        val result = createRunner()
            .withEnvironment(mapOf("JAVA_HOME" to "foo"))
            .withArguments("tasks")
            .buildAndFail()
        assertThat(result.output).contains("Check for more details here!")
    }

    @Test
    fun testFailAssembleMultipleProjects() {
        assumeSupportedVersion()
        assumeCanRunAndroidBuild()
        testProjectRoot.newFile("local.properties").writeText("sdk.dir=${androidHome()}\n")
        writeBuildGradle(
            """
            buildscript {
              repositories {
                google()
              }
              dependencies {
                classpath("com.android.tools.build:gradle:$agpVersion")
              }
            }

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
            """.trimIndent()
        )

        testProjectRoot.writeFileToName(
            "settings.gradle",
            """
            include 'app-one'
            include 'app-two'
        """.trimMargin()
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
              compileSdkVersion 28
            }
            """.trimIndent()
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
              compileSdkVersion 28
            }
            """.trimIndent()
        )
        val result = createRunner()
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
               """.trimMargin()
        )
    }

    @Test
    fun testFailInstallMultipleProjects() {
        assumeSupportedVersion()
        assumeCanRunAndroidBuild()
        testProjectRoot.newFile("local.properties").writeText("sdk.dir=${androidHome()}\n")
        writeBuildGradle(
            """
            buildscript {
              repositories {
                google()
              }
              dependencies {
                classpath("com.android.tools.build:gradle:$agpVersion")
              }
            }

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
            """.trimIndent()
        )

        testProjectRoot.writeFileToName(
            "settings.gradle",
            """
            include 'app-one'
            include 'app-two'
        """.trimMargin()
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
              compileSdkVersion 28
            }
            """.trimIndent()
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
              compileSdkVersion 28
            }
            """.trimIndent()
        )
        val result = createRunner()
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
               """.trimMargin()
        )
    }

    @Test
    fun testFailOnEmptyDirectories() {
        assumeSupportedVersion()
        assumeEmptyDirectoriesInInput()
        writeBuildGradle(
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
                    |}
                """.trimMargin("|")
        )
        val fixtureName = "java-fixture"
        testProjectRoot.newFile("settings.gradle").writeText("include '$fixtureName'")
        testProjectRoot.setupFixture(fixtureName)
        testProjectRoot.newFolder("java-fixture", "src", "main", "java", "com", "foo")

        val result = createRunner()
            .withArguments("assemble")
            .buildAndFail()

        assertThat(result.output).contains("Empty src dir(s) found. This causes build cache misses. Run the following command to fix it.")
    }

    @Test
    fun testDontFailOnEmptyDirectoriesWhenDisabled() {
        assumeSupportedVersion()
        writeBuildGradle(
            """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  disallowMultipleDaemons = false
                    |  javaHome {
                    |    ensureJavaHomeMatches = false
                    |  }
                    |  failOnEmptyDirectories = false
                    |  warnWhenNotUsingParallelGC = false
                    |}
                """.trimMargin("|")
        )
        val fixtureName = "java-fixture"
        testProjectRoot.newFile("settings.gradle").writeText("include '$fixtureName'")
        testProjectRoot.setupFixture(fixtureName)
        testProjectRoot.newFolder("java-fixture", "src", "main", "java", "com", "foo")

        val result = createRunner()
            .withArguments("assemble")
            .build()

        assertThat(result.output).contains("SUCCESS")
    }

    private fun createRunner(): GradleRunner {
        return GradleRunner.create()
            .withProjectDir(testProjectRoot.root)
            .withPluginClasspath()
            .withGradleVersion(version)
    }

    private fun assumeEmptyDirectoriesInInput() {
        Assume.assumeTrue(GradleVersion.version(version) < GradleVersion.version("6.8"))
    }

    private fun assumeCanRunAndroidBuild() {
        Assume.assumeTrue(GradleVersion.version("5.4") < GradleVersion.version(version))
    }

    private fun assumeSupportedVersion() {
        Assume.assumeFalse(version == "6.0.1")
    }

    private fun assumeUnsupportedVersion() {
        Assume.assumeTrue(version == "6.0.1")
    }

    private fun writeBuildGradle(build: String) {
        testProjectRoot.writeBuildGradle(build)
    }

    private fun createFileInFolder(folder: File, fileName: String, contents: String) {
        File(folder, fileName).writeText(contents)
    }
}
