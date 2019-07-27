package com.osacky.doctor

import org.gradle.api.internal.tasks.compile.CompileJavaBuildOperationType

class JavaAnnotationTime(operationEvents: OperationEvents) {
    var totalDaggerTime = 0

    init {
        operationEvents.finishesOfType(CompileJavaBuildOperationType.Result::class.java)
                .filter { it.annotationProcessorDetails != null }
                .map { it.annotationProcessorDetails }
                .map { detailsList -> detailsList.filter { it.className.contains("dagger") }.sumBy { it.executionTimeInMillis.toInt() } }
                .subscribe {
                    totalDaggerTime += it
                }
    }

    fun onFinished() {
        println("total dagger time was $totalDaggerTime")
    }
}