package com.osacky.doctor.internal

import org.gradle.api.invocation.Gradle
import org.gradle.util.GradleVersion

fun Gradle.shouldUseCoCaClasses(): Boolean = GradleVersion.version(gradleVersion) >= GradleVersion.version("6.6")
