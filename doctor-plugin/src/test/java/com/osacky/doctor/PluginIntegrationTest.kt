package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class PluginIntegrationTest constructor(private val version: String) {
    @get:Rule val testProjectRoot = TemporaryFolder()

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun getParams(): List<String> {
            return listOf("5.0", "5.1", "5.2", "5.3", "5.4", "5.5.1")
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
                    |}
                """.trimMargin("|")
        )

        try {
            createRunner().buildAndFail()
        } catch (e: UnexpectedBuildFailure) {
            assertThat(e).hasMessageThat().contains("Must be using Gradle Version 5.1 in order to use DoctorPlugin. Current Gradle Version is Gradle $version")
        }
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
                    |}
                """.trimMargin("|")
        )
        try {
            createRunner().buildAndFail()
        } catch (e: UnexpectedBuildFailure) {
            assertThat(e).hasMessageThat()
                .contains(
                        "Daemons Active.\n" +
                        "This may indicate a settings mismatch between the IDE and the terminal.\n" +
                        "There might also be a bug causing extra Daemons to spawn.\n" +
                        "You can check active Daemons with `jps`.\n" +
                        "To kill all active Daemons use:\n" +
                        "pkill -f '.*GradleDaemon.*'\n" +
                        "\n" +
                        "This might be expected if you are working on multiple Gradle projects or if you are using build.gradle.kts.\n" +
                        "To disable this message add this to your root build.gradle file:\n" +
                        "doctor {\n" +
                        "  disallowMultipleDaemons = false\n" +
                        "}This might be expected if you are working on multiple Gradle projects.")
        }
    }

    @Test
    fun testFailMultipleProjects() {
        assumeSupportedVersion()
        Assume.assumeFalse("5.1" == version)
        writeBuildGradle("""
            buildscript {
              repositories {
                google()
              }
              dependencies {
                classpath("com.android.tools.build:gradle:3.4.2")
              }
            }
            
            plugins {
              id "com.osacky.doctor"
            }
            doctor {
              disallowMultipleDaemons = false
            }
        """.trimIndent())

        writeFileToName("settings.gradle", """
            include 'app-one'
            include 'app-two'
        """.trimMargin())

        val srcFolder = testProjectRoot.newFolder("app-one", "src", "main")
        val folder = File(testProjectRoot.root, "app-one")
        createFileInFolder(srcFolder, "AndroidManifest.xml", "<manifest package=\"com.foo.bar.one\"/>")
        createFileInFolder(folder, "build.gradle", """
            apply plugin: 'com.android.application'

            android {
              compileSdkVersion 28
            }
            """.trimIndent())
        val srcFolder2 = testProjectRoot.newFolder("app-two", "src", "main")
        val folder2 = File(testProjectRoot.root, "app-two")
        createFileInFolder(srcFolder2, "AndroidManifest.xml", "<manifest package=\"com.foo.bar.two\"/>")
        createFileInFolder(folder2, "build.gradle", """
            apply plugin: 'com.android.application'

            android {
              compileSdkVersion 28
            }
            """.trimIndent())
        try {
            createRunner()
                .withArguments("assembleDebug")
                .buildAndFail()
        } catch (e: UnexpectedBuildFailure) {
            assertThat(e).hasMessageThat().contains("Did you really mean to run all these? [task ':app-one:assembleDebug', task ':app-two:assembleDebug']\n" +
                    "Maybe you just meant to assemble one of them? In that case, you can try\n" +
                    "  ./gradlew app-one:assembleDebug\n" +
                    "Or did you hit \"build\" in the IDE (Green Hammer)? Did you know that assembles all the code in the entire project?\n" +
                    "Next time try \"Sync Project with Gradle Files\" (Gradle Elephant with Arrow).")
        }
    }

    private fun createRunner(): GradleRunner {
        return GradleRunner.create()
            .withProjectDir(testProjectRoot.root)
            .withPluginClasspath()
            .withGradleVersion(version)
    }

    private fun assumeSupportedVersion() {
        Assume.assumeFalse("5.0" == version)
    }

    private fun assumeUnsupportedVersion() {
        Assume.assumeTrue(version == "5.0")
    }

    private fun writeBuildGradle(build: String) {
        writeFileToName("build.gradle", build)
    }

    private fun writeFileToName(fileName: String, contents: String) {
        testProjectRoot.newFile(fileName).writeText(contents)
    }

    private fun createFileInFolder(folder: File, fileName: String, contents: String) {
        File(folder, fileName).writeText(contents)
    }
}
