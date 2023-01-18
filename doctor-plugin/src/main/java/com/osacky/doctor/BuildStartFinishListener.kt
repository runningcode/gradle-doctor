package com.osacky.doctor

interface BuildStartFinishListener {
    fun onStart()

    /**
     * Called when the build is finished to perform any clean up actions
     * @return A list of warnings to print out at the end of the build.
     */
    fun onFinish(): List<String>
}
