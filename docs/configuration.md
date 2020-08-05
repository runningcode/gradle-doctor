# Configuration

## Sample Configuration

=== "Groovy"
    ``` groovy
    doctor {
      /**
       * Throw an exception when multiple Gradle Daemons are running.
       */
      disallowMultipleDaemons = false
      /**
       * Ensure that we are using JAVA_HOME to build with this Gradle.
       */
      ensureJavaHomeMatches = true
      /**
       * Ensure we have JAVA_HOME set.
       */
      ensureJavaHomeIsSet = true
      /**
       * Show a message if the download speed is less than this many megabytes / sec.
       */
      downloadSpeedWarningThreshold = .5f
      /**
       * The level at which to warn when a build spends more than this percent garbage collecting.
       */
      GCWarningThreshold = 0.10f
      /**
       * Print a warning to the console if we spend more than this amount of time with Dagger annotation processors.
       */
      daggerThreshold = 5000
      /**
       * By default, Gradle caches test results. This can be dangerous if tests rely on timestamps, dates, or other files
       * which are not declared as inputs.
       */
      enableTestCaching = true
      /**
       * By default, Gradle treats empty directories as inputs to compilation tasks. This can cause cache misses.
       */
      failOnEmptyDirectories = true
      /**
       * Do not allow building all apps simultaneously. This is likely not what the user intended.
       */
      allowBuildingAllAndroidAppsSimultaneously = false
    }
    ```
=== "Kotlin"
    ``` groovy
    configure<DoctorExtension> {
      /**
       * Throw an exception when multiple Gradle Daemons are running.
       */
      disallowMultipleDaemons.set(false)
      /**
       * Ensure that we are using JAVA_HOME to build with this Gradle.
       */
      ensureJavaHomeMatches.set(true)
      /**
       * Ensure we have JAVA_HOME set.
       */
      ensureJavaHomeIsSet.set(true)
      /**
       * Show a message if the download speed is less than this many megabytes / sec.
       */
      downloadSpeedWarningThreshold.set(.5f)
      /**
       * The level at which to warn when a build spends more than this percent garbage collecting.
       */
      GCWarningThreshold.set(0.10f)
      /**
       * Print a warning to the console if we spend more than this amount of time with Dagger annotation processors.
       */
      daggerThreshold.set(5000)
      /**
       * By default, Gradle caches test results. This can be dangerous if tests rely on timestamps, dates, or other files
       * which are not declared as inputs.
       */
      enableTestCaching.set(true)
      /**
       * By default, Gradle treats empty directories as inputs to compilation tasks. This can cause cache misses.
       */
      failOnEmptyDirectories.set(true)
      /**
       * Do not allow building all apps simultaneously. This is likely not what the user intended.
       */
      allowBuildingAllAndroidAppsSimultaneously.set(false)
    }
    ```

[Configuration extension code is here.](https://github.com/runningcode/gradle-doctor/blob/master/doctor-plugin/src/main/java/com/osacky/doctor/DoctorExtension.kt)

