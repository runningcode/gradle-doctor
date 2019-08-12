plugins {
  kotlin("jvm")
  kotlin("kapt")
}

dependencies {
  implementation("com.google.dagger:dagger:2.24")
  kapt("com.google.dagger:dagger-compiler:2.24")

  implementation("com.google.auto.value:auto-value-annotations:1.6.2")
  kapt("com.google.auto.value:auto-value:1.6.2")
}
