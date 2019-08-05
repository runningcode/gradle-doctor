package com.osacky.doctor

open class DoctorExtension {
    /**
     * Throw an exception when multiple Gradle Daemons are running.
     */
    var disallowMultipleDaemons = true
    /**
     * Show a message if the download speed is less than this.
     */
    var downloadSpeedTrigger = 0.5f
    /**
     * The level at which to warn when a build
     */
    var GCWarningThreshold = 0.05f
}