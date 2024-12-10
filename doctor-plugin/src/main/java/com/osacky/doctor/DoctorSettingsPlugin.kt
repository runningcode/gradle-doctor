package com.osacky.doctor

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.create

class DoctorSettingsPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {

        val extension = target.extensions.create<DoctorExtension>("doctor")
        target.gradle.beforeProject {
            extensions.getByType(ExtraPropertiesExtension::class.java)
                .set(DoctorExtension.EXTRAS_KEY, extension)

            if (this.parent == null) {
                plugins.apply(DoctorPlugin::class.java)
            } else {
                plugins.apply(DoctorChildModulePlugin::class.java)
            }
        }
    }
}