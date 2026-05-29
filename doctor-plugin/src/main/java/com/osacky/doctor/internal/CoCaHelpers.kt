package com.osacky.doctor.internal

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal fun ProviderFactory.booleanGradleProperty(propertyName: String): Provider<Boolean> =
    gradleProperty(propertyName)
        .map { true }
        .orElse(false)
