pluginManagement {
  includeBuild("doctor-plugin")

  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("com.osacky.doctor")
  id("com.gradle.develocity") version "4.3"
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    termsOfUseAgree = "yes"
  }
}

doctor {
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

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
}
