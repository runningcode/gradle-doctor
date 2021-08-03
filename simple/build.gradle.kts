plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("junit:junit:4.13")
}

com.osacky.tagger.ScanApi(project).tag("test-tag")
