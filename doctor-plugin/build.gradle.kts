import org.gradle.api.attributes.plugin.GradlePluginApiVersion
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kgp)
    alias(libs.plugins.plugin.publish)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.maven.publish)
    `java-test-fixtures`
}

group = "com.osacky.doctor"
version = "0.12.1"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val parallelGCTest by sourceSets.creating
val integrationTest by sourceSets.creating

gradlePlugin {
    testSourceSets(integrationTest, parallelGCTest, sourceSets.test.get())
}

listOf("runtimeElements", "apiElements").forEach { configurationName ->
    configurations.named(configurationName).configure {
        attributes {
            attribute(
                GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
                objects.named(GradlePluginApiVersion::class.java, libs.versions.minGradle.get()),
            )
        }
    }
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin.lib)
    implementation(libs.rxjava)
    implementation(libs.develocity.agent.adapters)
    "parallelGCTestImplementation"(testFixtures(project))
    "integrationTestImplementation"(testFixtures(project))
    testFixturesApi(gradleTestKit())
    testFixturesApi(libs.junit)
    testFixturesApi(libs.truth)
    testFixturesApi(libs.mockito)
}

gradlePlugin {
    website.set("https://github.com/runningcode/gradle-doctor")
    vcsUrl.set("https://github.com/runningcode/gradle-doctor")
    plugins {
        create("doctor-plugin") {
            id = "com.osacky.doctor"
            displayName = "Doctor Plugin"
            description = "The right prescription for your gradle build."
            tags.addAll(listOf("doctor", "android"))
            implementationClass = "com.osacky.doctor.DoctorPlugin"
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    
    pom {
        name.set("Gradle Doctor")
        description.set("The right prescription for your Gradle build.")
        url.set("https://github.com/runningcode/gradle-doctor/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("runningcode")
                name.set("Nelson Osacky")
                url.set("https://github.com/runningcode/")
            }
        }
        scm {
            url.set("https://github.com/runningcode/gradle-doctor/")
            connection.set("scm:git:git://github.com/runningcode/gradle-doctor.git")
            developerConnection.set("scm:git:ssh://github.com/runningcode/gradle-doctor.git")
        }
    }
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

// PluginIntegrationTest covers Gradle 9.x via TestKit, which requires JDK 17+.
// Run it under a Java 17 launcher in its own task and exclude from the standard test task,
// which still runs unit tests under Java 11.
tasks.named<Test>("test").configure {
    exclude("com/osacky/doctor/PluginIntegrationTest*")
}

val pluginIntegrationTest = tasks.register<Test>("pluginIntegrationTest") {
    description = "Runs PluginIntegrationTest under a Java 17 launcher so Gradle 9.x can be exercised."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("com/osacky/doctor/PluginIntegrationTest*")
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
}

val java11Int = tasks.register<Test>("java11IntegrationTest") {
    group = "verification"
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(11))
    })
    testClassesDirs = parallelGCTest.output.classesDirs
    classpath = parallelGCTest.runtimeClasspath
}

tasks.check.configure { dependsOn(java11Int, integrationTestTask, pluginIntegrationTest) }

tasks.withType<ValidatePlugins>().configureEach {
    failOnWarning.set(true)
    enableStricterValidation.set(true)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        languageVersion= KotlinVersion.KOTLIN_1_8
        apiVersion = KotlinVersion.KOTLIN_1_8
    }
}
