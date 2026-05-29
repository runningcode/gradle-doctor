package com.osacky.doctor

import com.osacky.doctor.internal.CliCommandExecutor
import com.osacky.doctor.internal.Clock
import com.osacky.doctor.internal.DaemonChecker
import com.osacky.doctor.internal.DirtyBeanCollector
import com.osacky.doctor.internal.FRESH_DAEMON
import com.osacky.doctor.internal.IntervalMeasurer
import com.osacky.doctor.internal.PillBoxPrinter
import com.osacky.doctor.internal.SystemClock
import com.osacky.doctor.internal.UnixDaemonChecker
import com.osacky.doctor.internal.UnsupportedOsDaemonChecker
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.testing.Test
import org.gradle.internal.build.event.BuildEventListenerRegistryInternal
import org.gradle.internal.jvm.Jvm
import org.gradle.internal.service.UnknownServiceException
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.kotlin.dsl.withType
import org.gradle.launcher.daemon.server.scaninfo.DaemonScanInfo
import org.gradle.nativeplatform.platform.OperatingSystem
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

class DoctorPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        val extension = target.extensions.create<DoctorExtension>("doctor")

        val os: OperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()
        val providers = target.serviceOf<ProviderFactory>()
        val cliCommandExecutor = CliCommandExecutor(providers)
        val clock: Clock = SystemClock()
        val intervalMeasurer = IntervalMeasurer()
        val logger = Logging.getLogger(javaClass)
        val pillBoxPrinter = PillBoxPrinter(logger)
        val daemonChecker =
            BuildDaemonChecker(
                extension,
                createDaemonChecker(os, cliCommandExecutor),
                pillBoxPrinter,
            )
        val javaHomeCheck = createJavaHomeCheck(extension, pillBoxPrinter, providers)
        val appleRosettaTranslationCheck =
            AppleRosettaTranslationCheck(
                os,
                cliCommandExecutor,
                pillBoxPrinter,
                extension.appleRosettaTranslationCheckMode,
            )
        val garbagePrinter = GarbagePrinter(clock, DirtyBeanCollector(), extension)
        val buildOperations = getOperationEvents(target.gradle, extension)
        val javaAnnotationTime = JavaAnnotationTime(buildOperations, extension)
        val downloadSpeedMeasurer = DownloadSpeedMeasurer(buildOperations, extension, intervalMeasurer)
        val buildCacheConnectionMeasurer = BuildCacheConnectionMeasurer(buildOperations, extension, intervalMeasurer)
        val buildCacheKey = RemoteCacheEstimation((buildOperations as BuildOperations), target, providers, clock)
        val slowerFromCacheCollector = buildOperations.slowerFromCacheCollector()
        val jetifierWarning = JetifierWarning(extension, providers)
        val javaElevenGC = JavaGCFlagChecker(pillBoxPrinter, extension)
        val kotlinCompileDaemonFallbackDetector = KotlinCompileDaemonFallbackDetector(target.gradle, providers, extension)
        val list =
            listOf(
                daemonChecker,
                javaHomeCheck,
                garbagePrinter,
                javaAnnotationTime,
                downloadSpeedMeasurer,
                buildCacheConnectionMeasurer,
                buildCacheKey,
                slowerFromCacheCollector,
                jetifierWarning,
                javaElevenGC,
                kotlinCompileDaemonFallbackDetector,
            )

        garbagePrinter.onStart()
        javaAnnotationTime.onStart()
        downloadSpeedMeasurer.onStart()
        buildCacheConnectionMeasurer.onStart()
        buildCacheKey.onStart()
        slowerFromCacheCollector.onStart()
        target.gradle.projectsEvaluated {
            daemonChecker.onStart()
            javaHomeCheck.onStart()
            javaElevenGC.onStart()
            kotlinCompileDaemonFallbackDetector.onStart()
            appleRosettaTranslationCheck.onStart()
        }

        registerBuildFinishActions(list, pillBoxPrinter, target)

        tagFreshDaemon(target)

        val appProjectCollectorServiceName = "doctor-app-project-collector"
        val appProjectCollector =
            target.gradle.sharedServices.registerIfAbsent(
                appProjectCollectorServiceName,
                AppProjectCollectorService::class.java,
            ) {}

        target.gradle.lifecycle.beforeProject(
            ConfigureProjectAction(extension.enableTestCaching, appProjectCollectorServiceName),
        )

        target.gradle.taskGraph.whenReady {
            val appProjectPaths = appProjectCollector.get().getProjectPaths()
            // If there is only one application plugin, we don't need to check that we're assembling all the applications.
            if (appProjectPaths.size <= 1 || extension.allowBuildingAllAndroidAppsSimultaneously.get()) {
                return@whenReady
            }
            // Use Task.path (a String) so we don't need to access Task.project across project boundaries.
            val assembleTasksInAndroidAppProjects =
                allTasks
                    .filter {
                        val projectPath = it.path.projectPathFromTaskPath()
                        appProjectPaths.contains(projectPath) &&
                            (it.name.contains("assemble") || it.name.contains("install"))
                    }
            val projectsWithAssembleTasks =
                assembleTasksInAndroidAppProjects.map { it.path.projectPathFromTaskPath() }.toSet()
            // Check if we have at least one assemble task in every project which has the application plugin.
            if (projectsWithAssembleTasks.containsAll(appProjectPaths)) {
                val firstTask = assembleTasksInAndroidAppProjects[0]
                val firstProjectPath = firstTask.path.projectPathFromTaskPath()
                val firstProjectName =
                    if (firstProjectPath == ":") {
                        ""
                    } else {
                        firstProjectPath.substringAfterLast(":")
                    }
                val taskList =
                    assembleTasksInAndroidAppProjects.joinToString(
                        prefix = "[",
                        postfix = "]",
                    ) { "task '${it.path}'" }
                val errorMessage =
                    """
                    |Did you really mean to run all these? $taskList
                    |Maybe you just meant to assemble/install one of them? In that case, you can try
                    |  ./gradlew $firstProjectName:${firstTask.name}
                    |Or did you hit "build" in the IDE (Green Hammer)? Did you know that assembles all the code in the entire project?
                    |Next time try "Sync Project with Gradle Files" (Gradle Elephant with Arrow).
                """.trimMargin("|")
                throw GradleException(pillBoxPrinter.createPill(errorMessage))
            }
        }
    }

    /**
     * Extract the project path from an absolute task path.
     * `:foo:bar:baz` -> `:foo:bar`, `:bar` -> `:`.
     */
    private fun String.projectPathFromTaskPath(): String {
        val idx = lastIndexOf(':')
        return if (idx <= 0) ":" else substring(0, idx)
    }

    private fun createJavaHomeCheck(
        extension: DoctorExtension,
        pillBoxPrinter: PillBoxPrinter,
        providers: ProviderFactory,
    ): JavaHomeCheck {
        val jvmVariables =
            JvmVariables(
                environmentJavaHomeProvider = providers.environmentVariable(JAVA_HOME),
                gradleJavaHome = Jvm.current().javaHome.path,
            )
        return JavaHomeCheck(jvmVariables, extension.javaHomeHandler, pillBoxPrinter)
    }

    private fun tagFreshDaemon(settings: Settings) {
        val daemonScanInfo =
            try {
                settings.serviceOf<DaemonScanInfo>()
            } catch (_: UnknownServiceException) {
                null
            }
        if (daemonScanInfo?.numberOfBuilds == 1) {
            settings.withDevelocityPlugin {
                buildScan.tag(FRESH_DAEMON)
            }
        }
    }

    private fun registerBuildFinishActions(
        list: List<BuildStartFinishListener>,
        pillBoxPrinter: PillBoxPrinter,
        settings: Settings,
    ) {
        val runnable = TheActionThing(pillBoxPrinter, settings)

        val closeService =
            settings.gradle.sharedServices
                .registerIfAbsent("close-service", BuildFinishService::class.java) { }
                .get()
        closeService.closeMeWhenFinished {
            runnable.execute(list)
        }
    }

    private fun createDaemonChecker(
        operatingSystem: OperatingSystem,
        cliCommandExecutor: CliCommandExecutor,
    ): DaemonChecker =
        when {
            operatingSystem.isLinux || operatingSystem.isMacOsX -> UnixDaemonChecker(cliCommandExecutor)
            else -> UnsupportedOsDaemonChecker
        }

    private fun getOperationEvents(
        gradle: Gradle,
        extension: DoctorExtension,
    ): OperationEvents {
        val listenerService =
            gradle.sharedServices.registerIfAbsent(
                "listener-service",
                BuildOperationListenerService::class.java,
            ) {
                this.parameters
                    .getNegativeAvoidanceThreshold()
                    .set(extension.negativeAvoidanceThreshold)
            }
        val buildEventListenerRegistry = gradle.serviceOf<BuildEventListenerRegistryInternal>()
        buildEventListenerRegistry.onOperationCompletion(listenerService)
        return listenerService.get().getOperations()
    }

    class TheActionThing(
        private val pillBoxPrinter: PillBoxPrinter,
        private val settings: Settings,
    ) : Action<List<BuildStartFinishListener>> {
        override fun execute(list: List<BuildStartFinishListener>) {
            val thingsToPrint: List<String> =
                list.flatMap {
                    val messages = it.onFinish()
                    if (messages.isNotEmpty() && it is HasBuildScanTag) {
                        settings.withDevelocityPlugin {
                            it.addCustomValues(buildScan)
                        }
                    }
                    messages
                }
            if (thingsToPrint.isEmpty()) {
                return
            }

            pillBoxPrinter.writePrescription(thingsToPrint)
        }
    }
}
