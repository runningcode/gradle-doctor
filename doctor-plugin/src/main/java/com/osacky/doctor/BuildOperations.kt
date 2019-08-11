package com.osacky.doctor

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle
import org.gradle.internal.logging.events.StyledTextOutputEvent
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.BuildOperationListener
import org.gradle.internal.operations.BuildOperationListenerManager
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.operations.OperationProgressEvent
import org.gradle.internal.operations.OperationStartEvent

class BuildOperations(gradle: Gradle) : OperationEvents {

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

    private val Gradle.buildOperationListenerManger get() = (this as GradleInternal).services[BuildOperationListenerManager::class.java]

    override fun starts(): Observable<OperationStartEvent> = starts.hide()

    override fun finishes(): Observable<OperationFinishEvent> = finishes.hide()

    override fun progress(): Observable<OperationProgressEvent> = progress.hide()
}
