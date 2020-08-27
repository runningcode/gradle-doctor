package com.osacky.doctor

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.internal.tasks.compile.CompileJavaBuildOperationType
import org.gradle.internal.logging.events.operations.LogEventBuildOperationProgressDetails

class JavaAnnotationTime(
    private val operationEvents: OperationEvents,
    private val doctorExtension: DoctorExtension,
    private val buildscriptConfiguration: ConfigurationContainer
) : BuildStartFinishListener {
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

    override fun onFinish(): List<String> {
        disposable.dispose()
        if (totalDaggerTime > doctorExtension.daggerThreshold.get()) {
            val message = if (containsDelect()) enableReflectMessage else applyDelectPlugin
            return listOf("This build spent ${totalDaggerTime / 1000f} s in Dagger Annotation Processors.\n$message")
        }
        return emptyList()
    }

    private val applyDelectPlugin =
        """
        Use Dagger Reflect to skip Dagger Annotation processing:

        buildscript {
          classpath 'com.soundcloud.delect:delect-plugin:0.3.0'
        }
        apply plugin: 'com.soundcloud.delect'

        For more information: https://github.com/soundcloud/delect#usage
        """.trimIndent()

    private val enableReflectMessage =
        """
        Enable to Dagger Reflect to save yourself some time.
        echo "dagger.reflect=true" >> ~/.gradle/gradle.properties
        """.trimIndent()

    private fun containsDelect(): Boolean {
        return buildscriptConfiguration.getByName("classpath").incoming.dependencies.find { it.group == "com.soundcloud.delect" } != null
    }
}
