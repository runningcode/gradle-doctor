package com.osacky.doctor.internal

import org.gradle.api.provider.ProviderFactory
import org.gradle.util.GradleVersion
import java.util.Optional

fun isGradle74OrNewer(): Boolean = GradleVersion.current() >= GradleVersion.version("7.4")

fun sysProperty(
    name: String,
    providers: ProviderFactory,
): Optional<String> {
    if (!isGradle74OrNewer()) {
        val property = providers.systemProperty(name).forUseAtConfigurationTime()
        return Optional.ofNullable(property.orNull)
    }
    return Optional.ofNullable(System.getProperty(name))
}
