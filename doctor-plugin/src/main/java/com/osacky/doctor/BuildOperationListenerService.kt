package com.osacky.doctor

import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.BuildOperationListener
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.operations.OperationProgressEvent
import org.gradle.internal.operations.OperationStartEvent

abstract class BuildOperationListenerService : BuildService<BuildOperationListenerService.Params>, BuildOperationListener {

    interface Params : BuildServiceParameters {
        fun getNegativeAvoidanceThreshold(): Property<Int>
    }

    // Needs to be created within the service since the lifecycle of the BuildService is controlled by Gradle.
    private val buildOperations = BuildOperations(parameters.getNegativeAvoidanceThreshold())

    override fun started(buildOperation: BuildOperationDescriptor, startEvent: OperationStartEvent) {
        buildOperations.started(buildOperation, startEvent)
    }

    override fun progress(operationIdentifier: OperationIdentifier, progressEvent: OperationProgressEvent) {
        buildOperations.progress(operationIdentifier, progressEvent)
    }

    override fun finished(buildOperation: BuildOperationDescriptor, finishEvent: OperationFinishEvent) {
        buildOperations.finished(buildOperation, finishEvent)
    }

    fun getOperations(): OperationEvents {
        return buildOperations
    }
}
