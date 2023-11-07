package com.osacky.doctor

import com.google.common.truth.Truth.assertThat
import org.gradle.api.internal.provider.Providers
import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationType
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.OperationFinishEvent
import org.junit.Test

internal class SlowerFromCacheCollectorTest {
    val underTest = SlowerFromCacheCollector(Providers.of(0))

    @Test
    fun fasterToReExecuteTaskWarned() {
        underTest.onEvent(descriptorWithName("fasterToReExecute"), finishWithTime(5))

        assertThat(
            underTest.onFinish(),
        ).containsExactly(
            "The following operations were slower to pull from the cache than to rerun:\nfasterToReExecute\n" +
                "Consider disabling caching them.\n" +
                "For more information see: https://runningcode.github.io/gradle-doctor/slower-from-cache/",
        )
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

    @Test
    fun noWarningWhenUnderThreshold() {
        val thresholdCollector = SlowerFromCacheCollector(Providers.of(1000))
        thresholdCollector.onEvent(descriptorWithName("longButUnderThreshold"), finishWithTime(200))

        assertThat(thresholdCollector.onFinish()).isEmpty()
    }

    @Test
    fun warningWhenAboveThreshold() {
        val thresholdCollector = SlowerFromCacheCollector(Providers.of(1000))
        thresholdCollector.onEvent(descriptorWithName("fasterFromCacheAboveThreshold"), finishWithTime(10, 1020))

        assertThat(
            thresholdCollector.onFinish(),
        ).containsExactly(
            "The following operations were slower to pull from the cache than to rerun:\n" +
                "fasterFromCacheAboveThreshold\nConsider disabling caching them.\n" +
                "For more information see: https://runningcode.github.io/gradle-doctor/slower-from-cache/",
        )
    }

    fun finishWithTime(
        originExecutionTime: Long,
        thisExecutionTime: Long = 6,
    ): OperationFinishEvent {
        return OperationFinishEvent(0, thisExecutionTime, null, Result(originExecutionTime))
    }

    fun descriptorWithName(name: String): BuildOperationDescriptor {
        return BuildOperationDescriptor.displayName(name)
            .build()
    }

    internal class Result(private val originExecutionTime: Long) : ExecuteTaskBuildOperationType.Result {
        override fun getSkipMessage(): String? {
            throw NotImplementedError()
        }

        override fun getSkipReasonMessage(): String? {
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
