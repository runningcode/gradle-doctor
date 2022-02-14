plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(libs.junit)
}

com.osacky.tagger.ScanApi(project).tag("test-tag")
