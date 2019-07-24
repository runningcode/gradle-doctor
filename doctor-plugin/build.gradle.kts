plugins {
    `kotlin-dsl`
}

group = "com.osacky.doctor"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.12")
    testImplementation("com.google.truth:truth:1.0")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}