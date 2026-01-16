package com.osacky.doctor

import com.gradle.develocity.agent.gradle.adapters.DevelocityAdapter
import com.gradle.develocity.agent.gradle.adapters.develocity.DevelocityConfigurationAdapter
import com.gradle.develocity.agent.gradle.adapters.enterprise.GradleEnterpriseExtensionAdapter
import org.gradle.api.initialization.Settings

fun Settings.withDevelocityPlugin(block: DevelocityAdapter.() -> Unit) {
    plugins.withId("com.gradle.develocity") {
        val adapter = DevelocityConfigurationAdapter(extensions.getByName("develocity"))
        block(adapter)
    }
    plugins.withId("com.gradle.enterprise") {
        val adapter = GradleEnterpriseExtensionAdapter(extensions.getByName("gradleEnterprise"))
        block(adapter)
    }
}
