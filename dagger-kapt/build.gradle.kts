plugins {
  kotlin("jvm")
  kotlin("kapt")
}

dependencies {
  implementation("com.google.dagger:dagger:2.51")
  kapt("com.google.dagger:dagger-compiler:2.51")

  implementation("com.google.auto.value:auto-value-annotations:1.10.4")
  kapt("com.google.auto.value:auto-value:1.10.4")
}
