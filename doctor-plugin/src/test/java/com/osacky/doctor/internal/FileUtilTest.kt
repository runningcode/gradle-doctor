package com.osacky.doctor.internal

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FileUtilTest {

    @JvmField
    @Rule
    val tmpFolder = TemporaryFolder()

    @Test
    fun testMultipleLevels() {
        val base = tmpFolder.newFolder("base")
        tmpFolder.newFile("anotherfile.txt")
        val target = File(base, "nested/layers/here").apply { mkdirs() }

        val farthestParent = target.farthestEmptyParent()
        assertThat(farthestParent).isEqualTo(base)
    }

    @Test
    fun testSingleLevel() {
        val base = tmpFolder.newFolder("base")
        tmpFolder.newFile("anotherfile.txt")
        val target = File(base, "here").apply { mkdirs() }

        val farthestParent = target.farthestEmptyParent()
        assertThat(farthestParent).isEqualTo(base)
    }
}