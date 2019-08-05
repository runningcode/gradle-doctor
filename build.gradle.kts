import com.osacky.doctor.DoctorExtension

buildscript {
  repositories {
    mavenCentral()
    google()
  }

  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.41")
    classpath("com.android.tools.build:gradle:3.4.2")
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
  id("com.gradle.build-scan") version "2.3"
  id("com.github.ben-manes.versions") version "0.21.0"
}

apply(plugin = "idea")
apply(plugin = "com.osacky.doctor")

configure<DoctorExtension> {
  disallowMultipleDaemons = false
  GCWarningThreshold = 0.01f
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
  gradleVersion = "5.5.1"
}

buildScan {
  termsOfServiceUrl = "https://gradle.com/terms-of-service"
  termsOfServiceAgree = "yes"
  publishAlways()
}

tasks.register("testPlugin") {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":test"))
}
