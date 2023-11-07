package com.osacky.doctor.internal

import org.gradle.api.logging.Logger

/**
 * Prints strings inside a nicely fitted pill box.
 */
class PillBoxPrinter(private val logger: Logger) {
    private val messageLength = 100
    private val title = "Gradle Doctor Prescriptions"

    fun writePrescription(messages: List<String>) {
        if (messages.isNotEmpty()) {
            logger.warn(createTitle(messageLength))
        }

        messages.forEach { item ->
            logger.warn(padMessage(item))
            logger.warn(createEnding(messageLength))
        }
    }

    private fun createTitle(lineLength: Int): String {
        return " $title ".padStart(lineLength / 2 + 10, '=').padEnd(lineLength + 4, '=')
    }

    private fun createEnding(lineLength: Int): String {
        return "".padEnd(lineLength + 4, '=')
    }

    fun padMessage(message: String): String {
        return message.split('\n').flatMap { line ->
            val chunked = line.chunked(messageLength)
            if (chunked.isEmpty()) {
                return@flatMap listOf("| ${"".padEnd(messageLength)} |")
            } else {
                return@flatMap chunked.map { "| ${it.padEnd(messageLength)} |" }
            }
        }.joinToString("\n")
    }

    fun createPill(message: String): String {
        val lines = padMessage(message)
        val pilledLines = listOf(createTitle(messageLength)) + lines + createEnding(messageLength)
        val builder = StringBuilder()

        for (line in pilledLines) {
            builder.append(line).append('\n')
        }
        return builder.toString()
    }
}
