package com.osacky.doctor.internal

import org.gradle.api.logging.Logger

/**
 * Prints strings inside a nicely fitted pill box.
 */
class PillBoxPrinter(private val logger: Logger) {

    private val title = "Gradle Doctor Prescriptions"

    fun writePrescription(messages: List<String>) {
        val longestMessage = messages
            .flatMap { it.split('\n') }
            .maxBy { it.length }!!.length

        messages.forEachIndexed { index, item ->
            if (index == 0) {
                logger.warn(createTitle(longestMessage))
            }
            item.split('\n').forEach {
                logger.warn("| ${it.padEnd(longestMessage)} |")
            }
            logger.warn(createEnding(longestMessage))
        }
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
