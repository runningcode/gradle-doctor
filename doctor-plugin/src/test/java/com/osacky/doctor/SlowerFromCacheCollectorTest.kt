package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationType
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.OperationFinishEvent
import org.junit.Test

internal class SlowerFromCacheCollectorTest {

    val underTest = SlowerFromCacheCollector()

    @Test
    fun fasterToReExecuteTaskWarned() {
        underTest.onEvent(descriptorWithName("fasterToReExecute"), finishWithTime(5))

        assertThat(underTest.onFinish()).containsExactly("The following operations were slower to pull from the cache than to rerun:\nfasterToReExecute\nConsider disabling caching them.\nFor more information see: https://runningcode.github.io/gradle-doctor/slower-from-cache/")
    }

    @Test
    fun noWarningWhenSameAsCache() {
        underTest.onEvent(descriptorWithName("fasterFromCache"), finishWithTime(6))

        assertThat(underTest.onFinish()).isEmpty()
    }

    @Test
    fun noWarningWhenFasterFromCache() {
        underTest.onEvent(descriptorWithName("fasterFromCache"), finishWithTime(200))

        assertThat(underTest.onFinish()).isEmpty()
    }

    fun finishWithTime(originExecutionTime: Long): OperationFinishEvent {
        return OperationFinishEvent(0, 6, null, Result(originExecutionTime))
    }

    fun descriptorWithName(name: String): BuildOperationDescriptor {
        return BuildOperationDescriptor.displayName(name)
            .build()
    }

    internal class Result(private val originExecutionTime: Long) : ExecuteTaskBuildOperationType.Result {
        override fun getSkipMessage(): String? {
            throw NotImplementedError()
        }

        override fun isActionable(): Boolean {
            throw NotImplementedError()
        }

        override fun getOriginBuildInvocationId(): String? {
            throw NotImplementedError()
        }

        override fun getOriginExecutionTime(): Long? {
            return originExecutionTime
        }

        override fun getCachingDisabledReasonMessage(): String? {
            throw NotImplementedError()
        }

        override fun getCachingDisabledReasonCategory(): String? {
            throw NotImplementedError()
        }

        override fun getUpToDateMessages(): MutableList<String> {
            throw NotImplementedError()
        }

        override fun isIncremental(): Boolean {
            throw NotImplementedError()
        }
    }
}
