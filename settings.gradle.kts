pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.enterprise").version("3.7")
}

include("simple")
include("dagger-kapt")

includeBuild("doctor-plugin")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}
