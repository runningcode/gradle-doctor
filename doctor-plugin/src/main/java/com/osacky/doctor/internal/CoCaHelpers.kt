@file:Suppress("DEPRECATION")

package com.osacky.doctor.internal

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.util.GradleVersion

private val needsForUseAtConfigurationTime = GradleVersion.current() < GradleVersion.version("7.4")

internal fun ProviderFactory.environmentVariableCompat(propertyName: String): Provider<String> {
    val prop = environmentVariable(propertyName)
    return if (needsForUseAtConfigurationTime) {
        prop.forUseAtConfigurationTime()
    } else {
        prop
    }
}

internal fun ProviderFactory.systemPropertyCompat(propertyName: String): Provider<String> {
    val prop = systemProperty(propertyName)
    return if (needsForUseAtConfigurationTime) {
        prop.forUseAtConfigurationTime()
    } else {
        prop
    }
}

internal fun ProviderFactory.gradlePropertyCompat(propertyName: String): Provider<String> {
    val prop = gradleProperty(propertyName)
    return if (needsForUseAtConfigurationTime) {
        prop.forUseAtConfigurationTime()
    } else {
        prop
    }
}

internal fun ProviderFactory.booleanGradleProperty(propertyName: String): Provider<Boolean> =
    gradlePropertyCompat(propertyName)
        .map { true }
        .orElse(false)
