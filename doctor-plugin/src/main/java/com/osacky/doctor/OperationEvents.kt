package com.osacky.doctor

import io.reactivex.rxjava3.core.Observable
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.operations.OperationProgressEvent
import org.gradle.internal.operations.OperationStartEvent

interface OperationEvents {
    fun starts(): Observable<OperationStartEvent>

    fun progress(): Observable<OperationProgressEvent>

    fun finishes(): Observable<OperationFinishEvent>

    fun <T : Any> finishResultsOfType(clazz: Class<T>): Observable<T> =
        finishes()
            .filter { it.result != null }
            .map { it.result }
            .filter { clazz.isAssignableFrom(it!!::class.java) }
            .cast(clazz)
}
