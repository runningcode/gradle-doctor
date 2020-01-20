package com.osacky.doctor.internal

import org.gradle.api.logging.Logger

/**
 * Prints strings inside a nicely fitted pill box.
 */
class PillBoxPrinter(private val logger: Logger) {

    private val messageLength = 80
    private val title = "Gradle Doctor Prescriptions"

    fun writePrescription(messages: List<String>) {
        if (messages.isNotEmpty()) {
            logger.warn(createTitle(messageLength))
        }
        messages.forEach { item ->
            item.split('\n').forEachIndexed { _, line ->
                val chunked = line.chunked(messageLength)
                // If chunked is empty for empty lines, but we still want to print an empty line.
                if (chunked.isEmpty()) {
                    logger.warn("| ${"".padEnd(messageLength)} |")
                }
                line.chunked(messageLength).forEachIndexed { _, shortenedLine ->
                    logger.warn("| ${shortenedLine.padEnd(messageLength)} |")
                }
            }
        }
        logger.warn(createEnding(messageLength))
    }

    private fun createTitle(lineLength: Int): String {
        return " $title ".padStart(lineLength / 2 + 10, '=').padEnd(lineLength + 4, '=')
    }

    private fun createEnding(lineLength: Int): String {
        return "".padEnd(lineLength + 4, '=')
    }

    fun createPill(message: String): String {
        val longestLine = message.split('\n').maxBy { it.length }!!.length
        val messages = message.split('\n').map { "| ${it.padEnd(longestLine)} |" }
        val lines = listOf(createTitle(longestLine)) + messages + createEnding(longestLine)
        val builder = StringBuilder()

        for (line in lines) {
            builder.append(line).append('\n')
        }
        return builder.toString()
    }
}
