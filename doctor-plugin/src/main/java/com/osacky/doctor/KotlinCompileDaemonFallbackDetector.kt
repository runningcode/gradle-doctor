package com.osacky.doctor

import com.gradle.develocity.agent.gradle.adapters.BuildScanAdapter
import com.osacky.doctor.internal.KOTLIN_COMPILE_DAEMON_FALLBACK
import com.osacky.doctor.internal.systemPropertyCompat
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.ProviderFactory
import org.gradle.internal.logging.LoggingManagerInternal
import org.gradle.internal.logging.events.LogEvent
import org.gradle.internal.logging.events.OutputEvent
import org.gradle.internal.logging.events.OutputEventListener
import org.gradle.kotlin.dsl.support.serviceOf
import java.util.concurrent.atomic.AtomicInteger

class KotlinCompileDaemonFallbackDetector(
    private val gradle: Gradle,
    private val providers: ProviderFactory,
    private val extension: DoctorExtension,
) : BuildStartFinishListener,
    HasBuildScanTag {
    private val fallbackCounter = AtomicInteger(0)
    private val loggingService = gradle.serviceOf<LoggingManagerInternal>()
    private val failureEventListener = FailureEventListener(fallbackCounter)
    private val disposable = CompositeDisposable()

    override fun onStart() {
        if (!extension.warnIfKotlinCompileDaemonFallback.get() || isDaemonDisabled()) {
            return
        }
        loggingService.addOutputEventListener(failureEventListener)
    }

    override fun onFinish(): List<String> {
        loggingService.removeOutputEventListener(failureEventListener)
        disposable.dispose()
        if (hasUsedFallback()) {
            return listOf(
                """
                The Kotlin Compiler Daemon failed to connect and likely won't recover on its own.
                The fallback strategy is incredibly slow and should be avoided.
                https://youtrack.jetbrains.com/issue/KT-48843
                
                To recover, try killing the Kotlin Compiler Daemon:
                   1. ./gradlew --stop
                   2. Find Kotlin daemon process id (pid): `jps | grep Kotlin`
                   3. kill <pid>
                   
                If that didn't help, check that there are no invalid JVM arguments in "kotlin.daemon.jvm.options" property except for Xmx.
                """.trimIndent(),
            )
        }
        return emptyList()
    }

    override fun addCustomValues(buildScanApi: BuildScanAdapter) {
        if (hasUsedFallback()) {
            buildScanApi.tag(KOTLIN_COMPILE_DAEMON_FALLBACK)
        }
    }

    private fun hasUsedFallback() = fallbackCounter.get() > 1

    /**
     * Copy of internal logic in GradleKotlinCompilerRunner
     */
    private fun isDaemonDisabled(): Boolean {
        val strategy = providers.systemPropertyCompat("kotlin.compiler.execution.strategy").getOrElse("daemon")
        return strategy != "daemon" // "in-process", "out-of-process"
    }
}

internal class FailureEventListener(
    private val fallbacksCounter: AtomicInteger,
) : OutputEventListener {
    override fun onOutput(event: OutputEvent) {
        if (isFallbackMessage(event)) {
            // Can't fail a build from OutputEventListener. So, only mark it
            fallbacksCounter.incrementAndGet()
        }
    }

    private fun isFallbackMessage(event: OutputEvent): Boolean =
        event is LogEvent &&
            event.message.contains("Could not connect to kotlin daemon. Using fallback strategy.")
}
