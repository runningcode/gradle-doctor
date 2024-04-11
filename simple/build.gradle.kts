import com.osacky.doctor.findAdapter

plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(libs.junit)
}

findAdapter(project).tag("foo")