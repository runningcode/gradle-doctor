package com.osacky.doctor.internal

object UnsupportedOsDaemonChecker : DaemonChecker {

    /**
     * Non-suppressible build failure or warning log is non-actionable here,
     * it will only annoy users with unsupported platform.
     * mention in docs should be enough
     */
    override fun check(): String? = null
}
