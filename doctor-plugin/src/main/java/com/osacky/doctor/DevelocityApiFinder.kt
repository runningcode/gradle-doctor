package com.osacky.doctor

import com.gradle.develocity.agent.gradle.adapters.BuildResultAdapter
import com.gradle.develocity.agent.gradle.adapters.BuildScanAdapter
import com.gradle.develocity.agent.gradle.adapters.BuildScanCaptureAdapter
import com.gradle.develocity.agent.gradle.adapters.BuildScanObfuscationAdapter
import com.gradle.develocity.agent.gradle.adapters.PublishedBuildScanAdapter
import com.gradle.develocity.agent.gradle.adapters.develocity.DevelocityConfigurationAdapter
import com.gradle.develocity.agent.gradle.adapters.enterprise.GradleEnterpriseExtensionAdapter
import org.gradle.api.Action
import org.gradle.api.Project

fun findAdapter(project: Project): BuildScanAdapter {
    if (project.rootProject.extensions.findByName("develocity") != null) {
        return DevelocityConfigurationAdapter(project.rootProject.extensions.getByName("develocity")).buildScan
    } else if (project.rootProject.extensions.findByName("gradleEnterprise") != null) {
        return GradleEnterpriseExtensionAdapter(project.rootProject.extensions.getByName("gradleEnterprise")).buildScan
    }
    return NoOpBuildScanAdapter()
}

class NoOpBuildScanAdapter : BuildScanAdapter {
    override fun background(p0: Action<in BuildScanAdapter>?) {
    }

    override fun tag(p0: String?) {
    }

    override fun value(
        p0: String?,
        p1: String?,
    ) {
    }

    override fun link(
        p0: String?,
        p1: String?,
    ) {
    }

    override fun buildFinished(p0: Action<in BuildResultAdapter>?) {
    }

    override fun buildScanPublished(p0: Action<in PublishedBuildScanAdapter>?) {
    }

    override fun setTermsOfUseUrl(p0: String?) {
    }

    override fun getTermsOfUseUrl(): String? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setTermsOfUseAgree(p0: String?) {
    }

    override fun getTermsOfUseAgree(): String? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setUploadInBackground(p0: Boolean) {
    }

    override fun isUploadInBackground(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun publishAlways() {
    }

    override fun publishAlwaysIf(p0: Boolean) {
    }

    override fun publishOnFailure() {
    }

    override fun publishOnFailureIf(p0: Boolean) {
    }

    override fun getObfuscation(): BuildScanObfuscationAdapter? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun obfuscation(p0: Action<in BuildScanObfuscationAdapter>?) {
    }

    override fun getCapture(): BuildScanCaptureAdapter? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun capture(p0: Action<in BuildScanCaptureAdapter>?) {
        throw UnsupportedOperationException("not implemented")
    }
}
