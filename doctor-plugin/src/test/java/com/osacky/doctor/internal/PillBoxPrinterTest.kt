package com.osacky.doctor.internal

import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.gradle.api.logging.Logger
import org.junit.Test

internal class PillBoxPrinterTest {

    private val logger: Logger = mock()
    private val underTest = PillBoxPrinter(logger)

    @Test
    fun printSingleMessage() {

        val message =
            """
            |This is the message.
            |This is the second line.
        """.trimMargin()

        underTest.writePrescription(listOf(message))
        logger.inOrder {
            verify().warn("=============================== Gradle Doctor Prescriptions ============================================")
            verify().warn(
                """
                                || This is the message.                                                                                 |
                                || This is the second line.                                                                             |
                          """.trimMargin()
            )
            verify().warn("========================================================================================================")
        }
    }

    @Test
    fun printMultipleMessages() {

        val messageOne =
            """
            |This is the message.
            |This is the second line.
        """.trimMargin()

        val messageTwo =
            """
            |This is the message with a really long line that is really really really long and it's odd that the sentence keeps going without saying anything
            |
            |Just another message
        """.trimMargin()
        underTest.writePrescription(listOf(messageOne, messageTwo))
        logger.inOrder {
            verify().warn("=============================== Gradle Doctor Prescriptions ============================================")
            verify().warn(
                """
                                || This is the message.                                                                                 |
                                || This is the second line.                                                                             |
        """.trimMargin()
            )
            verify().warn("========================================================================================================")
            verify().warn(
                """
                                || This is the message with a really long line that is really really really long and it's odd that the  |
                                || sentence keeps going without saying anything                                                         |
                                ||                                                                                                      |
                                || Just another message                                                                                 |
        """.trimMargin()
            )
            verify().warn("========================================================================================================")
        }
    }

    @Test
    fun printLongMessage() {
        val longMessage =
            """
            |This is a really long message that will overflow from one line on to the other and looks really bad if we don't do anything about it and its also not such a well structured sentence but more of a run on.
        """.trimMargin()
        underTest.writePrescription(listOf(longMessage))
        logger.inOrder {
            verify().warn("=============================== Gradle Doctor Prescriptions ============================================")
            verify().warn(
                """
                                || This is a really long message that will overflow from one line on to the other and looks really bad  |
                                || if we don't do anything about it and its also not such a well structured sentence but more of a run  |
                                || on.                                                                                                  |
            """.trimMargin()
            )
            verify().warn("========================================================================================================")
        }
    }
}
