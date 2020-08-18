package com.osacky.doctor.internal

import java.io.File

/**
 * Returns the farthest empty parent directory above this directory. This is to solve the case where we may have
 * multiple empty directories and want to remove the root directory that contains all of them.
 *
 * In this case, [this] current directory must be completely empty. Parents are expected to only have one
 */
internal fun File.farthestEmptyParent(): File {
    check(isDirectory)
    check(listFiles().orEmpty().isEmpty())
    return generateSequence(this) { currentFile ->
        currentFile.parentFile?.takeIf { parent ->
            val parentFiles = parent.listFiles() ?: arrayOf(currentFile)
            parentFiles.size == 1 && parentFiles[0] == currentFile
        }
    }.last()
}
