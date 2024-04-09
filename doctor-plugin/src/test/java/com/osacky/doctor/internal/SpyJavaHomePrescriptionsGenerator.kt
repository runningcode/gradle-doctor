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

class SpyJavaHomePrescriptionsGenerator : JavaHomeCheckPrescriptionsGenerator {

    var noJavaHomeGenerationWasCalled = false
    var javaHomeMismatchGenerationWasCalled = false
    var capturedJavaHomeLocation: String? = null
    var capturedGradleJavaHomeLocation: String = ""
    override fun generateJavaHomeIsNotSetMessage(): String {
        noJavaHomeGenerationWasCalled = true
        return "Fake no java home message."
    }

    override fun generateJavaHomeMismatchesGradleHome(
        javaHomeLocation: String?,
        gradleJavaHomeLocation: String
    ): String {
        javaHomeMismatchGenerationWasCalled = true
        capturedJavaHomeLocation = javaHomeLocation
        capturedGradleJavaHomeLocation = gradleJavaHomeLocation
        return "Fake java mismatch prescription."
    }
}