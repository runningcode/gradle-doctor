pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.enterprise").version("3.5")
}

include("simple")
include("dagger-kapt")

includeBuild("doctor-plugin")

