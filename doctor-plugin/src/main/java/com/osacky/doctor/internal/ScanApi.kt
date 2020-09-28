package com.osacky.doctor.internal

import com.gradle.scan.plugin.BuildScanExtension
import org.gradle.api.Project

/**
 * Easily add tags to build scans iff the API is available.
 * No-op if the build scan API is not available.
 */
class ScanApi(project: Project) {
    // Only the root project will have the build scan extension
    private var extension: Any? = project.rootProject.extensions.findByName("buildScan")

    fun tag(tag: String) {
        extension.tag(tag)
    }

    fun value(name: String, value: String) {
        extension.value(name, value)
    }

    fun link(name: String, url: String) {
        extension.link(name, url)
    }

    fun Any?.tag(tag: String) {
        if (this == null) {
            return
        } else {
            (this as BuildScanExtension).tag(tag)
        }
    }

    fun Any?.value(name: String, value: String) {
        if (extension == null) {
            return
        }
        (extension as BuildScanExtension).value(name, value)
    }
    fun Any?.link(name: String, url: String) {
        if (extension == null) {
            return
        }
        (extension as BuildScanExtension).link(name, url)
    }
}
