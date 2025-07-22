import com.osacky.doctor.findAdapter

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    testImplementation(libs.junit)
}

findAdapter(project).tag("foo")