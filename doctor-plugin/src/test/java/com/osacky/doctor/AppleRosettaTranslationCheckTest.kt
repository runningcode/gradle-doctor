package com.osacky.doctor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.osacky.doctor.AppleRosettaTranslationCheckMode.DISABLED
import com.osacky.doctor.AppleRosettaTranslationCheckMode.ERROR
import com.osacky.doctor.AppleRosettaTranslationCheckMode.WARN
import com.osacky.doctor.internal.CliCommandExecutor
import com.osacky.doctor.internal.PillBoxPrinter
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.nativeplatform.platform.OperatingSystem
import org.junit.Test

class AppleRosettaTranslationCheckTest {
    private val operatingSystem = mock<OperatingSystem>()
    private val cliCommandExecutor = mock<CliCommandExecutor>()
    private val pillBoxPrinter = mock<PillBoxPrinter>()
    private val appleRosettaTranslationCheckMode = mock<Property<AppleRosettaTranslationCheckMode>>()

    private val underTest =
        AppleRosettaTranslationCheck(
            operatingSystem,
            cliCommandExecutor,
            pillBoxPrinter,
            appleRosettaTranslationCheckMode,
        )

    @Test
    fun testOperatingSystemCheck() {
        whenever(appleRosettaTranslationCheckMode.get()).doReturn(ERROR)

        underTest.onStart()

        verify(operatingSystem).isMacOsX
    }

    @Test
    fun testGradleRunsNotOnAppleNoException() {
        whenever(appleRosettaTranslationCheckMode.get()).doReturn(ERROR)
        whenever(operatingSystem.isMacOsX).doReturn(false)

        underTest.onStart()

        verifyZeroInteractions(cliCommandExecutor)
        verifyZeroInteractions(pillBoxPrinter)
    }

    @Test(expected = GradleException::class)
    fun testGradleRunsUnderAppleRosettaExceptionThrownWithMessage() {
        whenever(appleRosettaTranslationCheckMode.get()).doReturn(ERROR)
        whenever(operatingSystem.isMacOsX).doReturn(true)
        whenever(cliCommandExecutor.execute(underTest.isTranslatedCheckCommand, true))
            .thenReturn(underTest.translatedWithRosetta)

        underTest.onStart()

        verify(pillBoxPrinter).createPill(underTest.errorMessage)
    }

    @Test
    fun testGradleRunsNativelyOnAppleNoException() {
        whenever(appleRosettaTranslationCheckMode.get()).doReturn(ERROR)
        whenever(operatingSystem.isMacOsX).doReturn(true)
        whenever(cliCommandExecutor.execute(underTest.isTranslatedCheckCommand, true))
            .thenReturn("sysctl.proc_translated: 0")

        underTest.onStart()

        verifyZeroInteractions(pillBoxPrinter)
    }

    @Test
    fun testErrorDuringTranslationDeterminationNoException() {
        whenever(appleRosettaTranslationCheckMode.get()).doReturn(ERROR)
        whenever(operatingSystem.isMacOsX).doReturn(true)
        whenever(cliCommandExecutor.execute(underTest.isTranslatedCheckCommand, true))
            .thenReturn("sysctl.proc_translated: -1")

        underTest.onStart()

        verifyZeroInteractions(pillBoxPrinter)
    }

    @Test
    fun testNoExceptionWhenCheckIsDisabled() {
        whenever(appleRosettaTranslationCheckMode.get()).doReturn(DISABLED)
        whenever(operatingSystem.isMacOsX).doReturn(true)
        whenever(cliCommandExecutor.execute(underTest.isTranslatedCheckCommand, true))
            .thenReturn(underTest.translatedWithRosetta)

        underTest.onStart()

        verifyZeroInteractions(cliCommandExecutor)
        verifyZeroInteractions(pillBoxPrinter)
    }

    @Test
    fun testNoExceptionButWarningPrintedWhenCheckIsInWarnMode() {
        whenever(appleRosettaTranslationCheckMode.get()).doReturn(WARN)
        whenever(operatingSystem.isMacOsX).doReturn(true)
        whenever(cliCommandExecutor.execute(underTest.isTranslatedCheckCommand, true))
            .thenReturn(underTest.translatedWithRosetta)

        underTest.onStart()

        verify(pillBoxPrinter).writePrescription(listOf(underTest.errorMessage))
    }
}
