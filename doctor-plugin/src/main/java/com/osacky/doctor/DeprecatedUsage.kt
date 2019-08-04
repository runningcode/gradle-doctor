package com.osacky.doctor

class DeprecatedUsage(private val message: String, private val stackTraceElements: List<StackTraceElement>) {

    fun printStackTrace() {
        UsageException(message, stackTraceElements).printStackTrace()
    }

    private class UsageException(override val message: String, val stackTraceElements: List<StackTraceElement>) : Exception() {
        init {
            stackTrace = stackTraceElements.toTypedArray()
        }
    }
}