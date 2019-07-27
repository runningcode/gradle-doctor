plugins {
    `kotlin-dsl`
}

group = "com.osacky.doctor"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.reactivex.rxjava3:rxjava:3.0.0-RC1")
    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.12")
    testImplementation("com.google.truth:truth:1.0")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}