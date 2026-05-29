package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class IsolatedProjectsTest {
    @get:Rule
    val testProjectRoot = TemporaryFolder()

    @Test
    fun isolatedProjectsWorks() {
        val fixtureName = "java-fixture"
        testProjectRoot.writeSettingsGradle(
            """
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  javaHome {
                    |    disallowMultipleDaemons = false
                    |    ensureJavaHomeMatches = false
                    |  }
                    |  warnWhenNotUsingParallelGC = false
                    |}
                    |include '$fixtureName'
                """.trimMargin("|"),
        )
        testProjectRoot.setupFixture(fixtureName)

        val runner =
            GradleRunner
                .create()
                .forwardOutput()
                .withArguments(
                    "assemble",
                    "--configuration-cache",
                    "-Dorg.gradle.unsafe.isolated-projects=true",
                ).withProjectDir(testProjectRoot.root)
                .withPluginClasspath()

        val result = runner.build()

        assertThat(result.output).contains("Configuration cache entry stored.")
        assertThat(result.output).contains("BUILD SUCCESSFUL")

        val resultTwo = runner.build()
        assertThat(resultTwo.output).contains("Reusing configuration cache.")
        assertThat(resultTwo.output).contains("BUILD SUCCESSFUL")
    }

    /**
     * Validates that the `taskGraph.whenReady` multi-Android-app detection runs end-to-end
     * with Isolated Projects enabled. AGP 4.2.x (used by [PluginIntegrationTest]) is not
     * IP-compatible, so this test uses a stub plugin in buildSrc that registers itself with
     * id `com.android.application` so `plugins.withId(...)` in [DoctorPlugin] fires.
     */
    @Test
    fun failsAssembleMultipleProjectsUnderIsolatedProjects() {
        setupBuildSrcStubAndroidPlugin()

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
            |include 'app-one'
            |include 'app-two'
            """.trimMargin("|"),
        )

        writeAppModule("app-one")
        writeAppModule("app-two")

        val result =
            GradleRunner
                .create()
                .forwardOutput()
                .withProjectDir(testProjectRoot.root)
                .withPluginClasspath()
                .withArguments(
                    "assembleDebug",
                    "--configuration-cache",
                    "-Dorg.gradle.unsafe.isolated-projects=true",
                ).buildAndFail()

        assertThat(result.output).contains("Isolated projects is an incubating feature.")
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

    private fun writeAppModule(name: String) {
        testProjectRoot.newFolder(name)
        File(testProjectRoot.root, "$name/build.gradle").writeText(
            """
            plugins {
              id 'com.android.application'
            }
            """.trimIndent(),
        )
    }

    /**
     * Creates a buildSrc project that exposes a stub plugin with id
     * `com.android.application`. The plugin registers a no-op `assembleDebug` task so the
     * task graph contains the tasks the doctor plugin filters on.
     */
    private fun setupBuildSrcStubAndroidPlugin() {
        testProjectRoot.newFolder("buildSrc")
        File(testProjectRoot.root, "buildSrc/build.gradle").writeText(
            """
            plugins {
              id 'java-gradle-plugin'
            }
            repositories {
              mavenCentral()
            }
            gradlePlugin {
              plugins {
                fakeAndroidApp {
                  id = 'com.android.application'
                  implementationClass = 'FakeAndroidAppPlugin'
                }
              }
            }
            """.trimIndent(),
        )
        testProjectRoot.newFolder("buildSrc", "src", "main", "java")
        File(testProjectRoot.root, "buildSrc/src/main/java/FakeAndroidAppPlugin.java").writeText(
            """
            import org.gradle.api.Plugin;
            import org.gradle.api.Project;

            public class FakeAndroidAppPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    project.getTasks().register("assembleDebug");
                }
            }
            """.trimIndent(),
        )
    }
}
