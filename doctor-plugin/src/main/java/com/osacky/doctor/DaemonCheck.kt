package com.osacky.doctor


class DaemonCheck {

    fun numberOfDaemons() : Int {
        return arrayOf("/bin/bash", "-c", "ps aux | grep GradleDaemon | wc -l").execute().toInt() - 2
    }

    private fun Array<String>.execute() : String {
        val process = Runtime.getRuntime().exec(this)
        if (process.waitFor() != 0) {
            throw RuntimeException(process.errorStream.readBytes().toString(Charsets.UTF_8).trim())
        }

        return process.inputStream.readBytes().toString(Charsets.UTF_8).trim()
    }
}