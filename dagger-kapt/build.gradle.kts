plugins {
  kotlin("jvm")
  kotlin("kapt")
}

dependencies {
  implementation("com.google.dagger:dagger:2.43.1")
  kapt("com.google.dagger:dagger-compiler:2.42")

  implementation("com.google.auto.value:auto-value-annotations:1.9")
  kapt("com.google.auto.value:auto-value:1.9")
}
