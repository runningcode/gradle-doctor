import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  alias(libs.plugins.kgp)
  alias(libs.plugins.versions)
}

tasks.withType(Test::class.java).configureEach {
  testLogging {
    events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
  }
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
  dependsOn(gradle.includedBuild("doctor-plugin").task(":publishToMavenCentral"))
}

tasks.register("publishToMavenLocal").configure {
  dependsOn(gradle.includedBuild("doctor-plugin").task(":publishToMavenLocal"))
}
