package com.osacky.doctor

import com.gradle.develocity.agent.gradle.adapters.BuildScanAdapter

interface HasBuildScanTag {
    /**
     * Called when onFinish() returns an non empty result to ask for custom values to be printed.
     */
    fun addCustomValues(buildScanApi: BuildScanAdapter)
}
