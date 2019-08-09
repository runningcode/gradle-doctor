package com.osacky.doctor.internal

import org.gradle.api.Project

class DLogger(private val project: Project) {

    fun log(message: String) {
        project.logger.lifecycle(message)
    }
}