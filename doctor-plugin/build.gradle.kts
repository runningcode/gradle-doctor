import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `kotlin-dsl`
    // Keep at 1.3.72 until https://github.com/pinterest/ktlint/issues/771 is resolved.
    kotlin("jvm") version "1.3.72"
    id("com.gradle.plugin-publish") version "0.12.0"
    id("org.jmailen.kotlinter") version "2.4.1"
    `maven-publish`
    signing
}

group = "com.osacky.doctor"
version = "0.4.3-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.0")
    implementation("io.reactivex.rxjava3:rxjava:3.0.2")
    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.13")
    testImplementation("com.google.truth:truth:1.0.1")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
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


kotlinDslPluginOptions {
    experimentalWarning.set(false)
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

tasks.withType(Test::class.java).configureEach {
    jvmArgs("-XX:+HeapDumpOnOutOfMemoryError")
    testLogging {
        events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
    }
}
