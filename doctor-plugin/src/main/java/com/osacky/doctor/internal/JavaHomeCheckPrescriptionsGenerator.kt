/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osacky.doctor.internal

internal const val NO_JAVA_HOME = "JAVA_HOME is not set."
internal const val JAVA_HOME_AT_LOCATION = "JAVA_HOME is %s"
internal const val GRADLE_JAVA_HOME_AT_LOCATION = "Gradle is using %s"
internal const val NO_JAVA_HOME_MESSAGE = """
    $NO_JAVA_HOME
    Please set JAVA_HOME so that switching between Android Studio and the terminal does not trigger a full rebuild.
    To set JAVA_HOME: (using bash)
    echo "export JAVA_HOME=${'$'}(/usr/libexec/java_home)" >> ~/.bash_profile
    or `~/.zshrc` if using zsh.
    %s
"""

internal const val JAVA_HOME_DOESNT_MATCH_GRADLE_HOME = """
    Gradle is not using JAVA_HOME.
    %s
    %s
    This can slow down your build significantly when switching from Android Studio to the terminal.
    To fix: Project Structure -> JDK Location.
    Set this to your JAVA_HOME.
    %s
"""

interface JavaHomeCheckPrescriptionsGenerator {
    fun generateJavaHomeIsNotSetMessage(): String

    fun generateJavaHomeMismatchesGradleHome(
        javaHomeLocation: String?,
        gradleJavaHomeLocation: String,
    ): String
}

internal class DefaultPrescriptionGenerator(private val extraMessage: () -> String?) :
    JavaHomeCheckPrescriptionsGenerator {
    override fun generateJavaHomeIsNotSetMessage() = String.format(NO_JAVA_HOME_MESSAGE, extraMessage().orEmpty()).trimIndent()

    override fun generateJavaHomeMismatchesGradleHome(
        javaHomeLocation: String?,
        gradleJavaHomeLocation: String,
    ): String {
        val javaHomeMessage =
            javaHomeLocation?.let { String.format(JAVA_HOME_AT_LOCATION, it) } ?: NO_JAVA_HOME
        val gradleJavaHomeMessage = String.format(GRADLE_JAVA_HOME_AT_LOCATION, gradleJavaHomeLocation)
        return String.format(
            JAVA_HOME_DOESNT_MATCH_GRADLE_HOME,
            javaHomeMessage,
            gradleJavaHomeMessage,
            extraMessage().orEmpty(),
        ).trimIndent()
    }
}
