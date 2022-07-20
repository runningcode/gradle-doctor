package com.osacky.doctor.internal

import java.io.InputStream

class CliCommandExecutor {

    fun execute(command: Array<String>): String {
        val process = Runtime.getRuntime().exec(command)
        if (process.waitFor() != 0) {
            throw RuntimeException(process.errorStream.readToString())
        }
        return process.inputStream.readToString()
    }

    private fun InputStream.readToString() = use {
        it.readBytes().toString(Charsets.UTF_8).trim()
    }
}
