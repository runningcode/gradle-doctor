/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osacky.doctor

import com.nhaarman.mockitokotlin2.*
import com.osacky.doctor.internal.PillBoxPrinter
import com.osacky.doctor.internal.SpyJavaHomePrescriptionsGenerator
import org.gradle.api.GradleException
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*

class JavaHomeCheckTest {

    @get:Rule
    val testProjectRoot = TemporaryFolder()

    private val allJavaHomeChecksEnabled = JavaHomeCheckConfig(
        ensureJavaHomeIsSet = true,
        ensureJavaHomeMatches = true,
        extraMessage = "Please ensure your that your java home is set and is the same as gradle java home.",
        failOnError = false
    )

    /**
     *  No need to test against different filesystems as this logic is entirely handled inside nio(Paths.get) framework,
     *  so it doesn't matter as the correct file separation char will be provided under the hood.
     */
    private val macOSContentFolderStructure = arrayOf("Contents", "Home")

    private val legitJavaHomePathFolders =
        arrayOf("Library", "Java", "JavaVirtualMachines", "x.y.z-jdk", *macOSContentFolderStructure)

    /**
     * Is used to simulate a different jdk path.
     */
    private val anotherLegitJavaHomePathFolders =
        arrayOf("Library", "Java", "JavaVirtualMachines", "z.y.x-jdk", *macOSContentFolderStructure)


    private val currentZuluDistributionFolder = "17.0.10-zulu"
    private val currentZuluJDKFolder = "zulu-17.jdk"
    private val zuluJDKStructure =
        arrayOf(currentZuluDistributionFolder, currentZuluJDKFolder, *macOSContentFolderStructure)
    private val zuluJavaHomePathFolders = arrayOf(
        "Users",
        "doctor",
        ".sdkman",
        "candidates",
        "java",
        *zuluJDKStructure,
    )

    private lateinit var javaHomePath: Path
    private lateinit var spyPrescriptionsGenerator: SpyJavaHomePrescriptionsGenerator
    private lateinit var underTest: JavaHomeCheck

    private val pillBoxPrinter = mock<PillBoxPrinter>()

    @Before
    fun setup() {
        javaHomePath = setupJavaHomePathStructure(legitJavaHomePathFolders)
        spyPrescriptionsGenerator = SpyJavaHomePrescriptionsGenerator()
    }

    @Test
    fun `given environment java home path is not set when a full check is performed then there are at least two prescriptions`() {
        val jvmVariables = setupIdenticalJvmVariables().copy(environmentJavaHome = null)
        val config = allJavaHomeChecksEnabled
        underTest =
            JavaHomeCheck(
                jvmVariables,
                config,
                pillBoxPrinter,
                spyPrescriptionsGenerator
            )
        underTest.onStart()
        val errors = underTest.onFinish()
        assertTrue(errors.size == 2)
        assertTrue(spyPrescriptionsGenerator.noJavaHomeGenerationWasCalled)
        assertTrue(spyPrescriptionsGenerator.javaHomeMismatchGenerationWasCalled)
        verifyJvmVariablesAreUsedForPrescriptionGeneration(jvmVariables)
    }

    @Test(expected = GradleException::class)
    fun `given gradle and environment java home path doesn't match when a failOnError check is performed then there is at least one prescription and an exception thrown`() {
        val jvmVariables = setupDifferentJvmVariables()
        val config = allJavaHomeChecksEnabled.copy(failOnError = true)
        underTest = JavaHomeCheck(jvmVariables, config, pillBoxPrinter, spyPrescriptionsGenerator)
        underTest.onStart()
        assertFalse(spyPrescriptionsGenerator.noJavaHomeGenerationWasCalled)
        assertTrue(spyPrescriptionsGenerator.javaHomeMismatchGenerationWasCalled)
        verifyJvmVariablesAreUsedForPrescriptionGeneration(jvmVariables)
        verify(pillBoxPrinter, atLeast(1)).createPill(any())
    }

    @Test
    fun `given gradle and environment java home path doesn't match when a full check is performed then there is at least one prescription`() {
        val jvmVariables = setupDifferentJvmVariables()
        val config = allJavaHomeChecksEnabled
        underTest = JavaHomeCheck(jvmVariables, config, pillBoxPrinter, spyPrescriptionsGenerator)
        underTest.onStart()
        val errors = underTest.onFinish()
        assertTrue(errors.isNotEmpty())
        assertFalse(spyPrescriptionsGenerator.noJavaHomeGenerationWasCalled)
        assertTrue(spyPrescriptionsGenerator.javaHomeMismatchGenerationWasCalled)
        verifyJvmVariablesAreUsedForPrescriptionGeneration(jvmVariables)
    }

    @Test
    fun `given gradle and environment java home matches when a full check is performed then there are no prescriptions`() {
        val legitJvmVariables = setupIdenticalJvmVariables()
        underTest = JavaHomeCheck(legitJvmVariables, allJavaHomeChecksEnabled, pillBoxPrinter)
        underTest.onStart()
        val errors = underTest.onFinish()
        verifyZeroInteractions(pillBoxPrinter)
        assertTrue(errors.isEmpty())
    }

    // in order to run this test successfully on windows you need to run as administrator as you need extra permissions to create symlinks
    @Test
    fun `given zulu like java distributions(with symlinks) where gradle and environment java home matches when a full check is performed then there are no prescriptions`() {
        javaHomePath = setupJavaHomePathStructure(zuluJavaHomePathFolders)

        // removing 17.0.10-zulu/Contents/Home/ to reach to root Users/doctor/.sdkman/candidates/java and setup symlink
        val amountOfDirectoriesBeforeRootJavaFolder = zuluJDKStructure.size
        val javaDistributionsRootFolderPath = javaHomePath.root.resolve(
            javaHomePath.subpath(
                0,
                javaHomePath.count() - amountOfDirectoriesBeforeRootJavaFolder
            )
        )

        // creating several distributions to make it look more realistic
        val severalJavaDistributions = arrayOf("11.0.21.fx-zulu", "17.0.10-amzn", "17.0.10-oracle")
        severalJavaDistributions.forEach {
            javaDistributionsRootFolderPath.resolve(it).createDirectory()
        }
        val zuluRootFolderPath = javaHomePath.root.resolve(
            javaHomePath.subpath(
                0,
                javaHomePath.count() - (amountOfDirectoriesBeforeRootJavaFolder - 1)
            )
        )

        // creating the sdkman "current" symlink and pointing to zulu
        val sdkmanEnvironmentJavaHome = javaDistributionsRootFolderPath.resolve("current")
            .createSymbolicLinkPointingTo(zuluRootFolderPath.toAbsolutePath())
        val javaExecutableFolder = javaHomePath.resolve(JAVA_EXECUTABLES_FOLDER).createDirectories()

        // creating zulu bin symlink and pointing it to the actual /Contents/Home/bin folder
        zuluRootFolderPath.resolve(JAVA_EXECUTABLES_FOLDER)
            .createSymbolicLinkPointingTo(javaExecutableFolder.toAbsolutePath())

        // environmentJavaHome=***/Users/doctor/.sdkman/candidates/java/current and gradleJavaHome=***/Users/doctor/.sdkman/candidates/java/17.0.10-zulu/zulu-17.jdk/Contents/Home
        val jvmVariables = JvmVariables(sdkmanEnvironmentJavaHome.pathString, javaHomePath.pathString)
        underTest = JavaHomeCheck(jvmVariables, allJavaHomeChecksEnabled, pillBoxPrinter, spyPrescriptionsGenerator)
        underTest.onStart()
        val errors = underTest.onFinish()
        assertFalse(spyPrescriptionsGenerator.noJavaHomeGenerationWasCalled)
        assertFalse(spyPrescriptionsGenerator.javaHomeMismatchGenerationWasCalled)
        assertTrue(errors.isEmpty())
    }

    private fun setupJavaHomePathStructure(folders: Array<String>): Path {
        return Paths.get(testProjectRoot.root.path, *folders).also {
            it.createDirectories()
        }
    }

    private fun setupIdenticalJvmVariables(): JvmVariables {
        val legitPath = javaHomePath.pathString
        return JvmVariables(legitPath, legitPath)
    }

    private fun setupDifferentJvmVariables(): JvmVariables {
        val anotherJavaHomePath = setupJavaHomePathStructure(anotherLegitJavaHomePathFolders)
        return JvmVariables(javaHomePath.toString(), anotherJavaHomePath.toString())
    }

    private fun verifyJvmVariablesAreUsedForPrescriptionGeneration(jvmVariables: JvmVariables) {
        assertEquals(jvmVariables.environmentJavaHome, spyPrescriptionsGenerator.capturedJavaHomeLocation)
        assertEquals(jvmVariables.gradleJavaHome, spyPrescriptionsGenerator.capturedGradleJavaHomeLocation)
    }
}
