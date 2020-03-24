package com.osacky.doctor

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.tasks.SnapshotTaskInputsBuildOperationType
import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationType
import org.gradle.api.invocation.Gradle
import org.gradle.internal.hash.HashCode
import org.gradle.internal.logging.events.StyledTextOutputEvent
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.BuildOperationListener
import org.gradle.internal.operations.BuildOperationListenerManager
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.operations.OperationProgressEvent
import org.gradle.internal.operations.OperationStartEvent

class BuildOperations(gradle: Gradle) : OperationEvents {

    // TODO move this out of this class
    private val snapshotIdsMap = HashMap<OperationIdentifier, SnapshotTaskInputsBuildOperationType.Result>()
    private val executeTaskIdsMap = HashMap<OperationIdentifier, ExecuteTaskBuildOperationType.Result>()

    private val starts: PublishSubject<OperationStartEvent> = PublishSubject.create()
    private val progress: PublishSubject<OperationProgressEvent> = PublishSubject.create()
    private val finishes: PublishSubject<OperationFinishEvent> = PublishSubject.create()
    private val listener = object : BuildOperationListener {
        override fun progress(operationIdentifier: OperationIdentifier, progressEvent: OperationProgressEvent) {
            // There's a ton of these. Don't pass them through for better performance.
            if (progressEvent.details is StyledTextOutputEvent) {
                return
            }
            progress.onNext(progressEvent)
        }

        override fun finished(buildOperation: BuildOperationDescriptor, finishEvent: OperationFinishEvent) {
            finishes.onNext(finishEvent)

            if (finishEvent.result is ExecuteTaskBuildOperationType.Result) {
                executeTaskIdsMap[buildOperation.id!!] = finishEvent.result as ExecuteTaskBuildOperationType.Result
            }
            if (finishEvent.result is SnapshotTaskInputsBuildOperationType.Result) {
                snapshotIdsMap[buildOperation.parentId!!] = finishEvent.result as SnapshotTaskInputsBuildOperationType.Result
            }
        }

        override fun started(buildOperation: BuildOperationDescriptor, startEvent: OperationStartEvent) {
            starts.onNext(startEvent)
        }
    }

    init {
        gradle.buildOperationListenerManger.addListener(listener)
        gradle.buildFinished {
            gradle.buildOperationListenerManger.removeListener(listener)
        }
    }

    // TODO move this out of this class
    fun hashes(): List<HashCode> {
        val hashes = mutableListOf<HashCode>()
        executeTaskIdsMap.entries.forEach { entry ->
            if (entry.value.skipMessage == null) {
                if (snapshotIdsMap.containsKey(entry.key)) {
                    hashes.add(HashCode.fromBytes(snapshotIdsMap[entry.key]!!.hashBytes!!))
                }
            }
        }
        return hashes
    }

    private val Gradle.buildOperationListenerManger get() = (this as GradleInternal).services[BuildOperationListenerManager::class.java]

    override fun starts(): Observable<OperationStartEvent> = starts.hide()

    override fun finishes(): Observable<OperationFinishEvent> = finishes.hide()

    override fun progress(): Observable<OperationProgressEvent> = progress.hide()
}
