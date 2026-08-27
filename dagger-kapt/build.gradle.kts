plugins {
  kotlin("jvm")
  kotlin("kapt")
}

dependencies {
  implementation(libs.kotlin.stdlib)
  implementation(libs.dagger)
  kapt(libs.dagger.compiler)

  implementation(libs.auto.value.annotations)
  kapt(libs.auto.value)
}
