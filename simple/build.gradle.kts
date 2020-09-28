plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("junit:junit:4.13")
}

com.osacky.doctor.internal.ScanApi(project).tag("test-tag")
