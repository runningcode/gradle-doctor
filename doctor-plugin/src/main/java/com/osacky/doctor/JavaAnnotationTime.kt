package com.osacky.doctor

import com.gradle.develocity.agent.gradle.adapters.BuildScanAdapter
import com.osacky.doctor.internal.LONG_DAGGER
import com.osacky.doctor.internal.plusAssign
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.gradle.api.internal.tasks.compile.CompileJavaBuildOperationType

class JavaAnnotationTime(
    private val operationEvents: OperationEvents,
    private val doctorExtension: DoctorExtension,
) : BuildStartFinishListener,
    HasBuildScanTag {
    private var totalDaggerTime = 0

    private val disposable = CompositeDisposable()

    override fun onStart() {
        disposable +=
            operationEvents
                .finishResultsOfType(CompileJavaBuildOperationType.Result::class.java)
                .filter { it.annotationProcessorDetails != null }
                .map { it.annotationProcessorDetails }
                .map { detailsList -> detailsList.filter { it.className.contains("dagger") }.sumBy { it.executionTimeInMillis.toInt() } }
                .subscribe {
                    totalDaggerTime += it
                }
    }

    override fun onFinish(): List<String> {
        disposable.dispose()
        if (totalDaggerTime > doctorExtension.daggerThreshold.get()) {
            return listOf("This build spent ${totalDaggerTime / 1000f} s in Dagger Annotation Processors.")
        }
        return emptyList()
    }

    override fun addCustomValues(buildScanApi: BuildScanAdapter) {
        buildScanApi.tag(LONG_DAGGER)
    }
}
