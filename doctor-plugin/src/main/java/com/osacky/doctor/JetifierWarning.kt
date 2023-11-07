package com.osacky.doctor

import org.gradle.api.Project

class JetifierWarning(private val doctorExtension: DoctorExtension, private val project: Project) : BuildStartFinishListener {
    override fun onStart() {
    }

    override fun onFinish(): List<String> {
        val isJetifierEnabled = project.findProperty(JETIFIER_GRADLE_PROPERTY) as String?
        if (doctorExtension.warnWhenJetifierEnabled.get() &&
            isJetifierEnabled != null &&
            isJetifierEnabled == "true"
        ) {
            return listOf(
                """
                Jetifier was enabled which means your builds are slower by 4-20%.
                Here's an article to help you disable it:
                https://adambennett.dev/2020/08/disabling-jetifier/

                To disable this warning, configure the Gradle Doctor extension:
                doctor {
                  warnWhenJetifierEnabled.set(false)
                }
                """.trimIndent(),
            )
        } else {
            return emptyList()
        }
    }

    companion object {
        const val JETIFIER_GRADLE_PROPERTY = "android.enableJetifier"
    }
}
