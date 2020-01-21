plugins {
  `java-library`
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.google.dagger:dagger:2.25.4")
  annotationProcessor("com.google.dagger:dagger-compiler:2.25.4")

  implementation("com.google.auto.value:auto-value-annotations:1.6.2")
  annotationProcessor("com.google.auto.value:auto-value:1.6.2")
}
