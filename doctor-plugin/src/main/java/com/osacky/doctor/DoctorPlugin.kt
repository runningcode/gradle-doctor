package com.osacky.doctor

import com.gradle.develocity.agent.gradle.adapters.BuildScanAdapter
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
import com.osacky.doctor.internal.isGradle74OrNewer
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.GradleInternal
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.testing.Test
import org.gradle.internal.build.event.BuildEventListenerRegistryInternal
import org.gradle.internal.jvm.Jvm
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.kotlin.dsl.withType
import org.gradle.launcher.daemon.server.scaninfo.DaemonScanInfo
import org.gradle.nativeplatform.platform.OperatingSystem
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.gradle.util.GradleVersion

class DoctorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        ensureMinimumSupportedGradleVersion()
        ensureAppliedInProjectRoot(target)

        val extension = target.extensions.create<DoctorExtension>("doctor")

        val os: OperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()
        val cliCommandExecutor = CliCommandExecutor(target)
        val clock: Clock = SystemClock()
        val intervalMeasurer = IntervalMeasurer()
        val pillBoxPrinter = PillBoxPrinter(target.logger)
        val daemonChecker = BuildDaemonChecker(extension, createDaemonChecker(os, cliCommandExecutor), pillBoxPrinter)
        val javaHomeCheck = createJavaHomeCheck(extension, pillBoxPrinter, target)
        val appleRosettaTranslationCheck =
            AppleRosettaTranslationCheck(
                os,
                cliCommandExecutor,
                pillBoxPrinter,
                extension.appleRosettaTranslationCheckMode,
            )
        val garbagePrinter = GarbagePrinter(clock, DirtyBeanCollector(), extension)
        val buildOperations = getOperationEvents(target, extension)
        val javaAnnotationTime = JavaAnnotationTime(buildOperations, extension)
        val downloadSpeedMeasurer = DownloadSpeedMeasurer(buildOperations, extension, intervalMeasurer)
        val buildCacheConnectionMeasurer = BuildCacheConnectionMeasurer(buildOperations, extension, intervalMeasurer)
        val buildCacheKey = RemoteCacheEstimation((buildOperations as BuildOperations), target, clock)
        val slowerFromCacheCollector = buildOperations.slowerFromCacheCollector()
        val jetifierWarning = JetifierWarning(extension, target)
        val javaElevenGC = JavaGCFlagChecker(pillBoxPrinter, extension)
        val kotlinCompileDaemonFallbackDetector = KotlinCompileDaemonFallbackDetector(target, extension)
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
        target.afterEvaluate {
            daemonChecker.onStart()
            javaHomeCheck.onStart()
            javaElevenGC.onStart()
            kotlinCompileDaemonFallbackDetector.onStart()
            appleRosettaTranslationCheck.onStart()
        }

        val buildScanApi = findAdapter(target)
        registerBuildFinishActions(list, pillBoxPrinter, target, buildScanApi)

        tagFreshDaemon(target, buildScanApi)

        val appPluginProjects = mutableSetOf<Project>()

        ensureNoCleanTaskDependenciesIfNeeded(target, extension, pillBoxPrinter)

        target.subprojects project@{
            // Ensure we are not caching any test tasks. Tests may not declare all inputs properly or depend on things like the date and caching them can lead to dangerous false positives.
            tasks.withType(Test::class.java).configureEach {
                if (!extension.enableTestCaching.get()) {
                    outputs.upToDateWhen { false }
                }
            }
            plugins.whenPluginAdded plugin@{
                if (this.javaClass.name == "com.android.build.gradle.AppPlugin") {
                    appPluginProjects.add(this@project)
                }
            }
        }

        target.gradle.taskGraph.whenReady {
            // If there is only one application plugin, we don't need to check that we're assembling all the applications.
            if (appPluginProjects.size <= 1 || extension.allowBuildingAllAndroidAppsSimultaneously.get()) {
                return@whenReady
            }
            val assembleTasksInAndroidAppProjects =
                allTasks
                    // Find executing tasks which are in Android AppPlugin Projects and contain the word assemble in the name.
                    .filter { appPluginProjects.contains(it.project) && it.name.contains("assemble") || it.name.contains("install") }
            val projectsWithAssembleTasks = assembleTasksInAndroidAppProjects.map { it.project }.toSet()
            // Check if we have at least one assemble task in every project which has the application plugin.
            if (projectsWithAssembleTasks.containsAll(appPluginProjects)) {
                val errorMessage =
                    """
                    |Did you really mean to run all these? $assembleTasksInAndroidAppProjects
                    |Maybe you just meant to assemble/install one of them? In that case, you can try
                    |  ./gradlew ${assembleTasksInAndroidAppProjects[0].project.name}:${assembleTasksInAndroidAppProjects[0].name}
                    |Or did you hit "build" in the IDE (Green Hammer)? Did you know that assembles all the code in the entire project?
                    |Next time try "Sync Project with Gradle Files" (Gradle Elephant with Arrow).
                """.trimMargin("|")
                throw GradleException(pillBoxPrinter.createPill(errorMessage))
            }
        }
    }

    private fun createJavaHomeCheck(
        extension: DoctorExtension,
        pillBoxPrinter: PillBoxPrinter,
        project: Project,
    ): JavaHomeCheck {
        val jvmVariables =
            JvmVariables(
                environmentJavaHomeProvider =
                    if (!isGradle74OrNewer()) {
                        project.providers.environmentVariable(JAVA_HOME).forUseAtConfigurationTime()
                    } else {
                        project.providers.environmentVariable(JAVA_HOME)
                    },
                gradleJavaHome = Jvm.current().javaHome.path,
            )
        return JavaHomeCheck(jvmVariables, extension.javaHomeHandler, pillBoxPrinter)
    }

    private fun tagFreshDaemon(
        target: Project,
        buildScanApi: BuildScanAdapter,
    ) {
        ((target.gradle as GradleInternal).services.find(DaemonScanInfo::class.java) as DaemonScanInfo?)?.let {
            if (it.numberOfBuilds == 1) {
                buildScanApi.tag(FRESH_DAEMON)
            }
        }
    }

    private fun registerBuildFinishActions(
        list: List<BuildStartFinishListener>,
        pillBoxPrinter: PillBoxPrinter,
        target: Project,
        buildScanApi: BuildScanAdapter,
    ) {
        val runnable = TheActionThing(pillBoxPrinter, buildScanApi)

        val closeService =
            target.gradle.sharedServices
                .registerIfAbsent("close-service", BuildFinishService::class.java) { }
                .get()
        closeService.closeMeWhenFinished {
            runnable.execute(list)
        }
    }

    private fun ensureNoCleanTaskDependenciesIfNeeded(
        target: Project,
        extension: DoctorExtension,
        pillBoxPrinter: PillBoxPrinter,
    ) {
        if (GradleVersion.current() >= GradleVersion.version("7.4")) {
            // Gradle 7.4 has a fix for 2488 and 10889
            return
        }
        target.allprojects {
            // We use afterEvaluate in case other plugins configure the Delete task. We want our configuration to happen last.
            afterEvaluate {
                tasks.withType(Delete::class).configureEach {
                    if (extension.disallowCleanTaskDependencies.get() && dependsOn.isNotEmpty()) {
                        val taskDependencies = dependsOn.map { it.toString() }
                        throw IllegalArgumentException(
                            pillBoxPrinter.createPill(
                                """
                                Adding dependencies to the clean task could cause unexpected build outcomes.
                                Please remove the dependency from $this on the following tasks: $taskDependencies. 
                                See github.com/gradle/gradle/issues/2488 for more information.
                                """.trimIndent(),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun ensureAppliedInProjectRoot(target: Project) {
        if (target.parent != null) {
            throw GradleException("Gradle Doctor must be applied in the project root.")
        }
    }

    private fun ensureMinimumSupportedGradleVersion() {
        if (GradleVersion.current() < GradleVersion.version("7.0")) {
            throw GradleException(
                "Must be using Gradle Version 7.0 in order to use DoctorPlugin. Current Gradle Version is ${GradleVersion.current()}",
            )
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
        target: Project,
        extension: DoctorExtension,
    ): OperationEvents {
        val listenerService =
            target.gradle.sharedServices.registerIfAbsent(
                "listener-service",
                BuildOperationListenerService::class.java,
            ) {
                this.parameters
                    .getNegativeAvoidanceThreshold()
                    .set(extension.negativeAvoidanceThreshold)
            }
        val buildEventListenerRegistry: BuildEventListenerRegistryInternal = target.serviceOf()
        buildEventListenerRegistry.onOperationCompletion(listenerService)
        return listenerService.get().getOperations()
    }

    class TheActionThing(
        private val pillBoxPrinter: PillBoxPrinter,
        private val buildScanApi: BuildScanAdapter,
    ) : Action<List<BuildStartFinishListener>> {
        override fun execute(list: List<BuildStartFinishListener>) {
            val thingsToPrint: List<String> =
                list.flatMap {
                    val messages = it.onFinish()
                    if (messages.isNotEmpty() && it is HasBuildScanTag) {
                        it.addCustomValues(buildScanApi)
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
