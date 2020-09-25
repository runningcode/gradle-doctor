buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.4.10"
}

group = "com.osacky.scans"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly("com.gradle:gradle-enterprise-gradle-plugin:3.4.1")
    compileOnly(gradleApi())
}
