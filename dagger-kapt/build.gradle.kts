plugins {
  kotlin("jvm")
  kotlin("kapt")
}

dependencies {
  implementation("com.google.dagger:dagger:2.27")
  kapt("com.google.dagger:dagger-compiler:2.27")

  implementation("com.google.auto.value:auto-value-annotations:1.7")
  kapt("com.google.auto.value:auto-value:1.7")
}
