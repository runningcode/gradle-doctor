plugins {
  kotlin("jvm")
  kotlin("kapt")
}

dependencies {
  implementation(libs.kotlin.stdlib)
  implementation("com.google.dagger:dagger:2.57.2")
  kapt("com.google.dagger:dagger-compiler:2.57.2")

  implementation("com.google.auto.value:auto-value-annotations:1.11.0")
  kapt("com.google.auto.value:auto-value:1.11.1")
}
