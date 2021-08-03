package com.osacky.doctor

import com.osacky.tagger.ScanApi

interface HasBuildScanTag {

    /**
     * Called when onFinish() returns an non empty result to ask for custom values to be printed.
     */
    fun addCustomValues(buildScanApi: ScanApi)
}
