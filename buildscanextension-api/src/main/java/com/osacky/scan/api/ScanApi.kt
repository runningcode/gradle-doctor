package com.osacky.scan.api

import com.gradle.scan.plugin.BuildScanExtension
import org.gradle.api.Action
import org.gradle.api.Project

/**
 * Easily add tags to build scans if the api is available.
 */
class ScanApi(project: Project) {
    private var extension: BuildScanExtension? = project.extensions.findByName("buildScan") as BuildScanExtension?

    fun background(action: Action<in BuildScanExtension>) {
        extension?.background(action)
    }

    fun tag(tag: String) {
        extension?.tag(tag)
    }

    fun value(name: String, value: String) {
        extension?.value(name, value)
    }

    fun link(name: String, url: String) {
        extension?.link(name, url)
    }
}
