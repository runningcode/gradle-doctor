import com.osacky.doctor.DoctorExtension
import org.gradle.api.tasks.testing.logging.TestLogEvent

// Upgrade transitive dependencies in plugin classpath
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        constraints {
            // The plugin com.github.ben-manes.versions:0.51.0 has dependency on com.squareup.okio:okio:3.2.0
            // which has reported vulnerability CVE-2023-3635. Use a newer version.
            classpath(libs.okio)
        }
    }
}

plugins {
  alias(libs.plugins.kgp)
  alias(libs.plugins.versions)
//  id("com.osacky.doctor")
}

//configure<DoctorExtension> {
//  disallowMultipleDaemons.set(false)
//  GCWarningThreshold.set(0.01f)
//  enableTestCaching.set(false)
//  downloadSpeedWarningThreshold.set(2.0f)
//  daggerThreshold.set(100)
//  javaHome {
//      ensureJavaHomeMatches.set(true)
//      ensureJavaHomeIsSet.set(true)
//  }
//}

tasks.withType(Test::class.java).configureEach {
  testLogging {
    events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
  }
}

tasks.wrapper {
  gradleVersion = "8.9"
}

tasks.register("pluginTasks").configure {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":tasks"))
}

tasks.register("checkPlugin").configure {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":check"))
}

tasks.register("lintKotlinPlugin").configure {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":lintKotlin"))
}

tasks.register("formatKotlinPlugin").configure {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":formatKotlin"))
}

tasks.register("testPlugin").configure {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":test"))
}

tasks.register("intTestPlugin").configure {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":integrationTest"))
}

tasks.register("publishToGradlePlugin").configure {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":publishPlugins"))
}

tasks.register("publishToMavenCentral").configure {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":publishAllPublicationsToMavenRepository"))
}

tasks.register("publishToMavenLocal").configure {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":publishToMavenLocal"))
}

