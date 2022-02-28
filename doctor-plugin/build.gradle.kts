import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kgp)
    alias(libs.plugins.plugin.publish)
    alias(libs.plugins.kotlinter)
    `maven-publish`
    signing
    `java-test-fixtures`
}

group = "com.osacky.doctor"
version = "0.8.1-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val parallelGCTest by sourceSets.creating
val integrationTest by sourceSets.creating

gradlePlugin {
    testSourceSets(integrationTest, parallelGCTest, sourceSets.test.get())
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin.lib)
    implementation(libs.tagger)
    implementation(libs.rxjava)
    "parallelGCTestImplementation"(testFixtures(project))
    "integrationTestImplementation"(testFixtures(project))
    testFixturesApi(gradleTestKit())
    testFixturesApi(libs.junit)
    testFixturesApi(libs.truth)
    testFixturesApi(libs.mockito)
}

pluginBundle {
    website = "https://github.com/runningcode/gradle-doctor"
    vcsUrl = "https://github.com/runningcode/gradle-doctor"
    tags = listOf("doctor", "android", "gradle")

    mavenCoordinates {
        artifactId = "doctor-plugin"
        groupId = project.group.toString()
    }
}

gradlePlugin {
    plugins {
        create("doctor-plugin") {
            id = "com.osacky.doctor"
            displayName = "Doctor Plugin"
            description = "The right prescription for your gradle build."
            implementationClass = "com.osacky.doctor.DoctorPlugin"
        }
    }
}

kotlinter {
  indentSize = 4
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

val isReleaseBuild : Boolean = !version.toString().endsWith("SNAPSHOT")

val sonatypeUsername : String? by project
val sonatypePassword : String? by project

publishing {
    repositories {
        repositories {
            maven {
                val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (isReleaseBuild) releasesRepoUrl else snapshotsRepoUrl
                credentials {
                    username = sonatypeUsername
                    password = sonatypePassword
                }
            }
        }
    }
    publications {
        afterEvaluate {
            named<MavenPublication>("pluginMaven") {
                signing.sign(this)
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])
                pom.configureForDoctor("Gradle Doctor")
            }
            named<MavenPublication>("doctor-pluginPluginMarkerMaven") {
                signing.sign(this)
                pom.configureForDoctor("Gradle Doctor")
            }
        }
    }
}

fun org.gradle.api.publish.maven.MavenPom.configureForDoctor(pluginName: String) {
    name.set(pluginName)
    description.set("The right prescription for your Gradle build.")
    url.set("https://github.com/runningcode/gradle-doctor")
    licenses {
        license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
    developers {
        developer {
            id.set("runningcode")
            name.set("Nelson Osacky")
        }
    }
    scm {
        connection.set("scm:git:git://github.com/runningcode/gradle-doctor.git")
        developerConnection.set("scm:git:ssh://github.com/runningcode/gradle-doctor.git")
        url.set("https://github.com/runningcode/gradle-doctor")
    }
}

signing {
    isRequired = isReleaseBuild
}

val integrationTestTask = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
}

tasks.withType(Test::class.java).configureEach {
    maxHeapSize = "1G"
    testLogging {
        events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
    }
}

val java8Int = tasks.register<Test>("java8IntegrationTest") {
    group = "verification"
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    })
    testClassesDirs = parallelGCTest.output.classesDirs
    classpath = parallelGCTest.runtimeClasspath
}
val java11Int = tasks.register<Test>("java11IntegrationTest") {
    group = "verification"
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(11))
    })
    testClassesDirs = parallelGCTest.output.classesDirs
    classpath = parallelGCTest.runtimeClasspath
}

tasks.check.configure { dependsOn(java8Int, java11Int, integrationTestTask)}

tasks.withType<ValidatePlugins>().configureEach {
    failOnWarning.set(true)
    enableStricterValidation.set(true)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

// Ensure Java 8 Compatibility
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
        languageVersion = "1.4"
        apiVersion = "1.4"
    }
}