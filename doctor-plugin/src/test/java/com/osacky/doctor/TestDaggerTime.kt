package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TestDaggerTime {
    @get:Rule
    val testProjectRoot = TemporaryFolder()

    @Test
    fun testDaggerJvm() {
        val fixtureName = "dagger-jvm"
        testProjectRoot.newFile("build.gradle").writeText("""
                    plugins {
                      id "com.osacky.doctor"
                    }
                    doctor {
                      disallowMultipleDaemons = false
                      daggerThreshold = 100
                      ensureJavaHomeMatches = !System.getenv().containsKey("CI")
                    }
        """.trimIndent())
        testProjectRoot.newFile("settings.gradle").writeText("""
            include '$fixtureName'
        """.trimIndent())

        testProjectRoot.setupFixture(fixtureName)

        val result = GradleRunner.create()
            .withProjectDir(testProjectRoot.root)
            .withPluginClasspath()
            .withGradleVersion("5.6")
            .withArguments("assemble")
            .build()

        assertThat(result.output).containsMatch("This build spent 0.\\d+ s in Dagger Annotation Processors.")
        assertThat(result.output).contains("""
            | Use Dagger Reflect to skip Dagger Annotation processing:                                             |
            |                                                                                                      |
            | buildscript {                                                                                        |
            |   classpath 'com.soundcloud.delect:delect-plugin:0.2.0'                                              |
            | }                                                                                                    |
            | apply plugin: 'com.soundcloud.delect'                                                                |
            |                                                                                                      |
            | For more information: https://github.com/soundcloud/delect#usage                                     |
            ========================================================================================================
            """.trimIndent()
        )
    }

    @Test
    fun testDaggerJvmWithDelect() {
        val fixtureName = "dagger-jvm"
        testProjectRoot.newFile("build.gradle").writeText("""
                    buildscript {
                      repositories {
                        mavenCentral()
                      }
                      dependencies {
                        classpath "com.soundcloud.delect:delect-plugin:0.2.0"
                      }
                    }
                    plugins {
                      id "com.osacky.doctor"
                    }
                    apply plugin: 'com.soundcloud.delect'
                    doctor {
                      disallowMultipleDaemons = false
                      daggerThreshold = 100
                      ensureJavaHomeMatches = false
                    }
        """.trimIndent())
        testProjectRoot.newFile("settings.gradle").writeText("""
            include '$fixtureName'
        """.trimIndent())

        testProjectRoot.setupFixture(fixtureName)

        val result = GradleRunner.create()
            .withProjectDir(testProjectRoot.root)
            .withPluginClasspath()
            .withGradleVersion("5.6")
            .withArguments("assemble")
            .build()

        assertThat(result.output).containsMatch("This build spent 0.\\d+ s in Dagger Annotation Processors.")
        assertThat(result.output).contains("""
            | Enable to Dagger Reflect to save yourself some time.                                                 |
            | echo "dagger.reflect=true" >> ~/.gradle/gradle.properties                                            |
            ========================================================================================================
            """.trimIndent())
    }
}
