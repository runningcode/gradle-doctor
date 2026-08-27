pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.develocity") version "4.5.0"
  id("com.gradle.common-custom-user-data-gradle-plugin") version "2.8.0"
}

val isCI = System.getenv("CI") != null

rootProject.name = "gradle-doctor"

develocity {
  server = "https://community.develocity.cloud"
  projectId = "runningcode"
  buildScan {
    uploadInBackground = !isCI
    publishing.onlyIf { it.isAuthenticated }
    obfuscation {
      ipAddresses { addresses -> addresses.map { _ -> "0.0.0.0" } }
    }
  }
}

buildCache {
  local {
    isEnabled = true
  }

  remote(develocity.buildCache) {
    isEnabled = true
    // Check access key presence to avoid build cache errors on PR builds when access key is not present
    val accessKey = System.getenv("DEVELOCITY_ACCESS_KEY")
    isPush = isCI && accessKey != null
  }
}

include("simple")
include("dagger-kapt")

includeBuild("doctor-plugin")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
}
