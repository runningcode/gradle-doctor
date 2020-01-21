package com.osacky.doctor

import com.osacky.doctor.internal.Finish
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import org.gradle.api.internal.tasks.compile.CompileJavaBuildOperationType
import org.gradle.internal.logging.events.operations.LogEventBuildOperationProgressDetails

class JavaAnnotationTime(private val operationEvents: OperationEvents, private val doctorExtension: DoctorExtension) : BuildStartFinishListener {
    private var totalDaggerTime = 0

    private val disposable = CompositeDisposable()

    override fun onStart() {
        disposable += operationEvents.finishResultsOfType(CompileJavaBuildOperationType.Result::class.java)
                .filter { it.annotationProcessorDetails != null }
                .map { it.annotationProcessorDetails }
                .map { detailsList -> detailsList.filter { it.className.contains("dagger") }.sumBy { it.executionTimeInMillis.toInt() } }
                .subscribe {
                    totalDaggerTime += it
                }

        disposable += operationEvents.progressDetailsOfType(LogEventBuildOperationProgressDetails::class.java)
            .subscribe {
                if (it.message.contains("kapt") && it.message.contains("dagger")) {
                    totalDaggerTime += "\\d+".toRegex().find(it.message)!!.groups[0]!!.value.toInt()
                }
            }
    }

    infix operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }

    override fun onFinish(): Finish {
        disposable.dispose()
        if (totalDaggerTime > doctorExtension.daggerThreshold) {
            return Finish.FinishMessage("This build spent ${totalDaggerTime / 1000f} s in Dagger Annotation Processors.\nSwitch to Dagger Reflect to save some time.")
        }
        return Finish.None
    }
}
