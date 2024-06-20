package com.osacky.doctor.internal

class UnixDaemonChecker(private val cliCommandExecutor: CliCommandExecutor) : DaemonChecker {
    override fun check(): String? {
        val numberOfDaemons = numberOfDaemons()
        return if (numberOfDaemons > 1) {
            """
            $numberOfDaemons Gradle Daemons Active.

            Multiple active Daemons can occur due to any of the following reasons:
            * Ongoing Gradle syncs
            * Simultaneous builds in different projects
            * Settings mismatches between the IDE and the terminal
            * Potential bug causing extra daemons to spawn
            
            To monitor active Daemons, use `jps`. 
            If needed, terminate all active Daemons with `pkill -f '.*GradleDaemon.*'`.
    
            Such a scenario is common when working on multiple Gradle projects or using 
            build.gradle.kts files.
            If this behavior is expected and not problematic, you can suppress this warning by updating your 
            root build.gradle file:
            doctor {
              disallowMultipleDaemons = false
            }
            """.trimIndent()
        } else {
            null
        }
    }

    private fun numberOfDaemons(): Int {
        return cliCommandExecutor
            .execute(arrayOf("/bin/bash", "-c", "ps aux | grep GradleDaemon | wc -l")).toInt() - 2
    }
}
