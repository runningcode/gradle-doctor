package com.osacky.doctor

import io.reactivex.disposables.Disposable
import org.gradle.api.internal.tasks.compile.CompileJavaBuildOperationType

class JavaAnnotationTime(private val operationEvents: OperationEvents) : BuildStartFinishListener {
    var totalDaggerTime = 0

    private lateinit var disposable: Disposable

    override fun onStart() {
        disposable = operationEvents.finishResultsOfType(CompileJavaBuildOperationType.Result::class.java)
                .filter { it.annotationProcessorDetails != null }
                .map { it.annotationProcessorDetails }
                .map { detailsList -> detailsList.filter { it.className.contains("dagger") }.sumBy { it.executionTimeInMillis.toInt() } }
                .subscribe {
                    totalDaggerTime += it
                }
    }

    override fun onFinish() {
        if (totalDaggerTime > 5000) {
            println("This build spent ${totalDaggerTime/1000f} s in Dagger Annotation Processors (Excluding Kapt).\nSwitch to Dagger Reflect to save some time.")
        }
    }
}