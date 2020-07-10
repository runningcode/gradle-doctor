import com.osacky.doctor.DoctorExtension
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
  repositories {
    mavenCentral()
    google()
  }

  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
    classpath("com.osacky.doctor:doctor-plugin:1.0")
  }
}

allprojects {
  repositories {
    mavenCentral()
    google()
  }
}

plugins {
  id("com.github.ben-manes.versions") version "0.28.0"
}

apply(plugin = "idea")
apply(plugin = "com.osacky.doctor")

configure<DoctorExtension> {
  disallowMultipleDaemons = false
  ensureJavaHomeMatches = !System.getenv().containsKey("CI")
  GCWarningThreshold = 0.01f
  enableTestCaching = false
  downloadSpeedWarningThreshold = 2.0f
  daggerThreshold = 100
}

tasks.withType(Test::class.java).configureEach {
  testLogging {
    events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
  }
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
  gradleVersion = "6.5.1"
}

buildScan {
  termsOfServiceUrl = "https://gradle.com/terms-of-service"
  termsOfServiceAgree = "yes"
  publishAlways()
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

tasks.register("publishToGradlePlugin").configure {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":publishPlugins"))
}

tasks.register("publishToMavenCentral").configure {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":publishMavenJavaPublicationToMavenRepository"))
}

tasks.register("publishToMavenLocal").configure {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":publishMavenJavaPublicationToMavenLocal"))
}

