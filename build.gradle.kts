import com.osacky.doctor.DoctorExtension

buildscript {
  repositories {
    mavenCentral()
    google()
    maven { url=uri("https://dl.bintray.com/kotlin/kotlin-eap") }
  }

  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50-eap-54")
    classpath("com.osacky.doctor:doctor-plugin:1.0")
  }
}

allprojects {
  repositories {
    mavenCentral()
    google()
    maven { url=uri("https://dl.bintray.com/kotlin/kotlin-eap") }
  }
}

plugins {
  id("com.gradle.build-scan") version "2.4"
  id("com.github.ben-manes.versions") version "0.22.0"
}

apply(plugin = "idea")
apply(plugin = "com.osacky.doctor")

configure<DoctorExtension> {
  disallowMultipleDaemons = false
  GCWarningThreshold = 0.01f
  enableTestCaching = false
  downloadSpeedWarningThreshold = 2.0f
  daggerThreshold = 100
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
  gradleVersion = "5.6"
}

buildScan {
  termsOfServiceUrl = "https://gradle.com/terms-of-service"
  termsOfServiceAgree = "yes"
  publishAlways()
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
