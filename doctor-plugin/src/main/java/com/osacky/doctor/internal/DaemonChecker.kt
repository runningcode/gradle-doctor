package com.osacky.doctor.internal

interface DaemonChecker {

    /**
     * @return error message or null if ok
     */
    fun check(): String?
}
