plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("junit:junit:4.13")
}

com.osacky.scan.tag.ScanApi(project).tag("test-tag")
