package com.osacky.doctor

import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.artifacts.DownloadArtifactBuildOperationType
import org.gradle.api.internal.tasks.compile.CompileJavaBuildOperationType
import org.gradle.api.invocation.Gradle
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.BuildOperationListener
import org.gradle.internal.operations.BuildOperationListenerManager
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.operations.OperationProgressEvent
import org.gradle.internal.operations.OperationStartEvent
import org.gradle.internal.resource.ExternalResourceListBuildOperationType
import org.gradle.internal.resource.ExternalResourceReadBuildOperationType
import org.gradle.internal.resource.ExternalResourceReadMetadataBuildOperationType

class JavaAnnotationTime {
    var totalDaggerTime = 0

    fun startListening(gradle: Gradle) {
        gradle.buildOperationListenerManager.addListener(listener)
    }

    fun onFinished(gradle: Gradle) {
        gradle.buildOperationListenerManager.removeListener(listener)
        println("total dagger time was $totalDaggerTime")
    }

    private val Gradle.buildOperationListenerManager: BuildOperationListenerManager
        get() = (this as GradleInternal).services[BuildOperationListenerManager::class.java]

    private val listener = object : BuildOperationListener {
        override fun progress(operationIdentifier: OperationIdentifier?, progressEvent: OperationProgressEvent?) {
        }

        override fun finished(buildOperation: BuildOperationDescriptor?, finishEvent: OperationFinishEvent) {
            when(finishEvent.result) {
                is ExternalResourceReadMetadataBuildOperationType.Result, is ExternalResourceListBuildOperationType.Result, is ExternalResourceReadBuildOperationType.Result, is DownloadArtifactBuildOperationType.Result   -> println(finishEvent.result)
            }
            if (finishEvent.result is ExternalResourceReadMetadataBuildOperationType.Result) {
                println()
            }
//            println("finish result ${finishEvent.result}")
            val result = finishEvent.result as? CompileJavaBuildOperationType.Result ?: return
            totalDaggerTime += result.annotationProcessorDetails?.filter { it.className.contains("dagger") }?.sumBy { it.executionTimeInMillis.toInt() } ?: 0
        }

        override fun started(buildOperation: BuildOperationDescriptor?, startEvent: OperationStartEvent?) {
        }
    }
}