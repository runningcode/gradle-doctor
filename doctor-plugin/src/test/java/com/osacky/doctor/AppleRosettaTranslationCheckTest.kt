package com.osacky.doctor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.osacky.doctor.internal.CliCommandExecutor
import com.osacky.doctor.internal.PillBoxPrinter
import org.gradle.api.GradleException
import org.gradle.nativeplatform.platform.OperatingSystem
import org.junit.Test

class AppleRosettaTranslationCheckTest {
    private val operatingSystem = mock<OperatingSystem>()
    private val cliCommandExecutor = mock<CliCommandExecutor>()
    private val pillBoxPrinter = mock<PillBoxPrinter>()

    private val underTest = AppleRosettaTranslationCheck(operatingSystem, cliCommandExecutor, pillBoxPrinter)

    @Test
    fun testOperatingSystemCheck() {
        underTest.onStart()

        verify(operatingSystem).isMacOsX
    }

    @Test
    fun testGradleRunsNotOnAppleNoException() {
        whenever(operatingSystem.isMacOsX).doReturn(false)

        underTest.onStart()

        verifyZeroInteractions(cliCommandExecutor)
        verifyZeroInteractions(pillBoxPrinter)
    }

    @Test(expected = GradleException::class)
    fun testGradleRunsUnderAppleRosettaExceptionThrownWithMessage() {
        whenever(operatingSystem.isMacOsX).doReturn(true)
        whenever(cliCommandExecutor.execute(underTest.isTranslatedCheckCommand))
            .thenReturn(underTest.translatedWithRosetta)

        underTest.onStart()

        verify(pillBoxPrinter).createPill(underTest.errorMessage)
    }

    @Test
    fun testGradleRunsNativelyOnAppleNoException() {
        whenever(operatingSystem.isMacOsX).doReturn(true)
        whenever(cliCommandExecutor.execute(underTest.isTranslatedCheckCommand))
            .thenReturn("sysctl.proc_translated: 0")

        underTest.onStart()

        verifyZeroInteractions(pillBoxPrinter)
    }

    @Test
    fun testErrorDuringTranslationDeterminationNoException() {
        whenever(operatingSystem.isMacOsX).doReturn(true)
        whenever(cliCommandExecutor.execute(underTest.isTranslatedCheckCommand))
            .thenReturn("sysctl.proc_translated: -1")

        underTest.onStart()

        verifyZeroInteractions(pillBoxPrinter)
    }
}
