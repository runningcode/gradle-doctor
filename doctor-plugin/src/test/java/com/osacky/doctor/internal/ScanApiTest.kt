package com.osacky.doctor.internal

import com.google.common.truth.Truth.assertThat
import com.osacky.doctor.setupFixture
import com.osacky.doctor.writeBuildGradle
import com.osacky.doctor.writeFileToName
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ScanApiTest {

    @get:Rule
    val testProjectRoot = TemporaryFolder()

    @Test
    fun scanApiBuildsWithoutEnterprisePlugin() {
        testProjectRoot.writeBuildGradle(
            """
                    |import com.osacky.doctor.internal.ScanApi
                    |plugins {
                    |  id "com.osacky.doctor"
                    |}
                    |doctor {
                    |  javaHome {
                    |    disallowMultipleDaemons = false
                    |    ensureJavaHomeMatches = false
                    |  }
                    |}
                    |new ScanApi(project).tag("hello")
                """.trimMargin("|")
        )
        val fixtureName = "java-fixture"
        testProjectRoot.writeFileToName("settings.gradle", "include '$fixtureName'")
        testProjectRoot.setupFixture(fixtureName)

        val result = GradleRunner.create()
            .forwardOutput()
            .withArguments("assemble")
            .withProjectDir(testProjectRoot.root)
            .withGradleVersion("6.6")
            .withPluginClasspath()
            .build()

        assertThat(result.output).contains("SUCCESS")
    }
}
