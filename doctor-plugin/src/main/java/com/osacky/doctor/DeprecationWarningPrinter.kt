package com.osacky.doctor

import io.reactivex.disposables.Disposable
import org.gradle.internal.featurelifecycle.DeprecatedUsageProgressDetails

class DeprecationWarningPrinter (private val operationEvents: OperationEvents) {

    private val depreacationTraces = mutableListOf<DeprecatedUsage>()

    fun start(): Disposable {
        return operationEvents.progressOfType(DeprecatedUsageProgressDetails::class.java)
                .subscribe {
                    depreacationTraces.add(DeprecatedUsage(it.summary, it.stackTrace))
                }
    }

    fun buildFinished() {
        depreacationTraces.forEach {
            it.printStackTrace()
        }
    }
}