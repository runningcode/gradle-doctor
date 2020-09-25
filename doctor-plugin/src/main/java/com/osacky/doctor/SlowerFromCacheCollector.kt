package com.osacky.doctor

import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationType
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.OperationFinishEvent

/**
 * Keeps track of which classes were slower to fetch from the cache than to re-run locally.
 */
class SlowerFromCacheCollector : BuildStartFinishListener, HasBuildScanTag {

    private val longerTaskList = mutableListOf<String>()

    fun onEvent(buildOperation: BuildOperationDescriptor, finishEvent: OperationFinishEvent) {
        val executeResult = finishEvent.result
        if (executeResult is ExecuteTaskBuildOperationType.Result) {
            val duration = finishEvent.endTime - finishEvent.startTime
            // If the current execution took longer than the original execution, let's print out a warning.
            if (executeResult.originExecutionTime != null && executeResult.originExecutionTime!! <= duration) {
                longerTaskList.add(buildOperation.displayName)
            }
        }
    }

    override fun onStart() {
    }

    override fun onFinish(): List<String> {
        if (longerTaskList.isEmpty()) {
            return emptyList()
        }
        return listOf("The following operations were slower to pull from the cache than to rerun:\n${longerTaskList.joinToString(separator = "\n")}\nConsider disabling caching them. For more information see: https://runningcode.github.io/gradle-doctor/slower-from-cache/")
    }

    override fun getTag(): String = "negative-savings"
}
