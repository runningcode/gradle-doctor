package com.osacky.doctor.internal

class UnixDaemonChecker(
    private val cliCommandExecutor: CliCommandExecutor,
) : DaemonChecker {
    override fun check(): String? {
        val numberOfDaemons = numberOfDaemons()
        return if (numberOfDaemons > 1) {
            """
            $numberOfDaemons Gradle Daemons Active.
            This may indicate a settings mismatch between the IDE and the terminal.
            There might also be a bug causing extra Daemons to spawn.
            You can check active Daemons with `jps`.
            To kill all active Daemons use:
            pkill -f '.*GradleDaemon.*'

            This might be expected if you are working on multiple Gradle projects or if you are using build.gradle.kts.
            To disable this message add this to your root build.gradle file:
            doctor {
              disallowMultipleDaemons = false
            }
            """.trimIndent()
        } else {
            null
        }
    }

    private fun numberOfDaemons(): Int =
        cliCommandExecutor
            .execute(arrayOf("/bin/bash", "-c", "ps aux | grep GradleDaemon | wc -l"))
            .toInt() - 2
}
