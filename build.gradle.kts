import com.osacky.doctor.DoctorExtension
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  kotlin("jvm") version "1.4.32"
  id("com.github.ben-manes.versions") version "0.38.0"
  id("com.osacky.doctor")
}

configure<DoctorExtension> {
  disallowMultipleDaemons.set(false)
  GCWarningThreshold.set(0.01f)
  enableTestCaching.set(false)
  downloadSpeedWarningThreshold.set(2.0f)
  daggerThreshold.set(100)
  javaHome {
    ensureJavaHomeMatches.set(!providers.environmentVariable("CI").forUseAtConfigurationTime().isPresent)
  }
}

tasks.withType(Test::class.java).configureEach {
  testLogging {
    events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
  }
}

tasks.wrapper {
  gradleVersion = "7.0"
}

buildScan {
  termsOfServiceUrl = "https://gradle.com/terms-of-service"
  termsOfServiceAgree = "yes"
  publishAlways()
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

