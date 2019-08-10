package com.osacky.doctor.internal

import org.gradle.api.logging.Logger

/**
 * Prints strings inside a nicely fitted pill box.
 */
class PillBoxPrinter(private val logger: Logger) {

    val title = "Gradle Doctor Prescriptions"

    fun print(messages: List<String>) {
        val longestMessage = messages
            .flatMap { it.split('\n') }
            .maxBy { it.length }!!.length

        messages.forEachIndexed { index, item ->
            if (index == 0) {
                logger.warn(" $title ".padStart(longestMessage / 2 + 10, '=').padEnd(longestMessage + 4, '='))
            }
            item.split('\n').forEach {
                logger.warn("| ${it.padEnd(longestMessage)} |")
            }
            logger.warn("".padEnd(longestMessage + 4, '='))
        }
    }
}
