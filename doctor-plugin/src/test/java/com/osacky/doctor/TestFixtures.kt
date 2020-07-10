package com.osacky.doctor

import org.junit.rules.TemporaryFolder
import java.io.File

fun TemporaryFolder.setupFixture(fixtureName: String) {
    File(this::class.java.classLoader.getResource(fixtureName)!!.file).copyRecursively(newFile(fixtureName), true)
}
