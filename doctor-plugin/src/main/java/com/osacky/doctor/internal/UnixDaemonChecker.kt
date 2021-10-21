package com.osacky.doctor.internal

import java.io.InputStream

class UnixDaemonChecker : DaemonChecker {

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

    private fun numberOfDaemons(): Int {
        return arrayOf("/bin/bash", "-c", "ps aux | grep GradleDaemon | wc -l").execute().toInt() - 2
    }

    private fun Array<String>.execute(): String {
        val process = Runtime.getRuntime().exec(this)
        if (process.waitFor() != 0) {
            throw RuntimeException(process.errorStream.readToString())
        }

        return process.inputStream.readToString()
    }

    private fun InputStream.readToString() = use {
        it.readBytes().toString(Charsets.UTF_8).trim()
    }
}
