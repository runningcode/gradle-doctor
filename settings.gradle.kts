import com.osacky.doctor.DoctorExtension

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
  includeBuild("doctor-plugin")
}

plugins {
  id("com.gradle.develocity") version "3.19.2"
  id("com.osacky.doctor")
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    termsOfUseAgree = "yes"
  }
}

configure<DoctorExtension> {
  disallowMultipleDaemons.set(false)
  GCWarningThreshold.set(0.01f)
  enableTestCaching.set(false)
  downloadSpeedWarningThreshold.set(2.0f)
  daggerThreshold.set(100)
  javaHome {
    ensureJavaHomeMatches.set(true)
    ensureJavaHomeIsSet.set(true)
  }
}

include("simple")
include("dagger-kapt")

includeBuild("doctor-plugin")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}
