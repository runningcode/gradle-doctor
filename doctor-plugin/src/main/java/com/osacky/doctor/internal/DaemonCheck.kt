package com.osacky.doctor.internal

import java.io.InputStream

class DaemonCheck {

    fun numberOfDaemons(): Int {
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