plugins {
  kotlin("jvm")
  kotlin("kapt")
}

dependencies {
  implementation("com.google.dagger:dagger:2.25.3")
  kapt("com.google.dagger:dagger-compiler:2.25.3")

  implementation("com.google.auto.value:auto-value-annotations:1.7")
  kapt("com.google.auto.value:auto-value:1.7")
}
