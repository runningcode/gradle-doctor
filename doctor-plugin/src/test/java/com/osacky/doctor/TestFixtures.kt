package com.osacky.doctor

import java.io.File
import org.junit.rules.TemporaryFolder

fun TemporaryFolder.setupFixture(fixtureName: String) {
    File(this::class.java.classLoader.getResource(fixtureName)!!.file).copyRecursively(newFile(fixtureName), true)
}
