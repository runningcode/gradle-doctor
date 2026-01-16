package com.osacky.doctor

import org.junit.rules.TemporaryFolder

fun TemporaryFolder.writeSettingsGradle(build: String) {
    writeFileToName("settings.gradle", build)
}

fun TemporaryFolder.writeBuildGradle(build: String) {
    writeFileToName("build.gradle", build)
}

fun TemporaryFolder.writeFileToName(
    fileName: String,
    contents: String,
) {
    newFile(fileName).writeText(contents)
}
