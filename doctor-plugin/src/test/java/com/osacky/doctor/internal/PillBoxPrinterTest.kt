package com.osacky.doctor.internal

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.gradle.api.logging.Logger
import org.junit.Test

internal class PillBoxPrinterTest {

    private val logger: Logger = mock()
    private val underTest = PillBoxPrinter(logger)

    @Test
    fun printSingleMessage() {

        val message = """
            |This is the message.
            |This is the second line.
        """.trimMargin()

        underTest.writePrescription(listOf(message))
        verifyPrinting("""
        | Gradle Doctor Prescriptions 
        || This is the message.     |
        || This is the second line. |
        |============================
        """.trimMargin("|"))
    }
    @Test
    fun printMultipleMessages() {

        val messageOne = """
            |This is the message.
            |This is the second line.
        """.trimMargin()

        val messageTwo = """
            |This is the message with a really long line that is really really really long and it's odd that the sentence keeps going without saying anything
            |
            |Just another message
        """.trimMargin()
        underTest.writePrescription(listOf(messageOne, messageTwo))
        verifyPrinting("""
        |===================================================== Gradle Doctor Prescriptions ==================================================================
        || This is the message.                                                                                                                             |
        || This is the second line.                                                                                                                         |
        |====================================================================================================================================================
        || This is the message with a really long line that is really really really long and it's odd that the sentence keeps going without saying anything |
        ||                                                                                                                                                  |
        || Just another message                                                                                                                             |
        |====================================================================================================================================================
        """.trimMargin("|"))
    }

    fun verifyPrinting(message: String) {
        var numberOfEqualsLines = 0
        message.split('\n').forEach { line ->
            if (line.length == line.count { it == '=' }) {
                numberOfEqualsLines++
                return@forEach
            }
            verify(logger).warn(line)
        }
    }
}
