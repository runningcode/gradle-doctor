package com.osacky.doctor

import com.osacky.doctor.internal.gradlePropertyCompat
import org.gradle.api.provider.ProviderFactory

class JetifierWarning(
    private val doctorExtension: DoctorExtension,
    private val providers: ProviderFactory,
) : BuildStartFinishListener {
    override fun onStart() {
    }

    override fun onFinish(): List<String> {
        val isJetifierEnabled = providers.gradlePropertyCompat(JETIFIER_GRADLE_PROPERTY).orNull
        if (doctorExtension.warnWhenJetifierEnabled.get() &&
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
