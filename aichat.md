23:39:01: Executing 'runIde --stacktrace'…

> Task :initializeIntelliJPlugin SKIPPED
> Task :patchPluginXml UP-TO-DATE
> Task :verifyPluginConfiguration
> Task :compileJava
> Task :generateBuildInfo UP-TO-DATE
> Task :processResources UP-TO-DATE
> Task :classes

> Task :instrumentCode
[ant:instrumentIdeaExtensions] /Users/muzi/Documents/Code_Class/IDEA_Class/commit-template-idea-plugin/src/main/java/com/c301/plugin/ui/CommitTemplateDialog.form: Class to bind does not exist: com.c301.plugin.ui.CommitTemplateDialog
[ant:instrumentIdeaExtensions] /Users/muzi/Documents/Code_Class/IDEA_Class/commit-template-idea-plugin/src/main/java/com/c301/plugin/ui/CommitTemplateSettingUI.form: Class to bind does not exist: com.c301.plugin.ui.CommitTemplateSettingUI
[ant:instrumentIdeaExtensions] /Users/muzi/Documents/Code_Class/IDEA_Class/commit-template-idea-plugin/src/main/java/com/c301/plugin/ui/EditCommitTypeDialog.form: Class to bind does not exist: com.c301.plugin.ui.EditCommitTypeDialog

> Task :instrumentedJar
> Task :jar
> Task :prepareSandbox

> Task :runIde
CompileCommand: exclude com/intellij/openapi/vfs/impl/FilePartNodeRoot.trieDescend bool exclude = true
2026-07-31 23:39:05,036 [    363]   WARN - #c.i.s.ComponentManagerImpl - `preload=true` must be used only for core services (service=com.jetbrains.rdserver.statistics.BackendStatisticsManager, plugin=com.jetbrains.codeWithMe)
2026-07-31 23:39:05,292 [    619]   WARN - #c.i.o.a.Application - No URL bundle (CFBundleURLTypes) is defined in the main bundle.
To be able to open external links, specify protocols in the app layout section of the build file.
Example: args.urlSchemes = ["your-protocol"] will handle following links: your-protocol://open?file=file&line=line
2026-07-31 23:39:05,305 [    632]   WARN - #c.i.u.n.s.ConfirmingTrustManager - Received an empty list of custom trusted root certificates from the system. Check log above for possible errors, enable debug logging in category 'org.jetbrains.nativecerts' for more information
2026-07-31 23:39:05,307 [    634]   WARN - #c.i.i.u.l.LafManagerImpl - VersionControl.Log.Commit.rowHeight = null in LookAndFeelThemeAdapter; it may lead to performance degradation
2026-07-31 23:39:05,387 [    714]   WARN - #c.i.s.ComponentManagerImpl - com.intellij.psi.search.FilenameIndex initializer requests com.intellij.ide.plugins.PluginUtil instance
2026-07-31 23:39:05,746 [   1073]   WARN - #c.i.s.ComponentManagerImpl - org.jetbrains.kotlin.idea.gradleJava.scripting.roots.GradleBuildRootDataSerializer initializer requests com.intellij.util.gist.storage.GistStorage instance
2026-07-31 23:39:06,193 [   1520]   WARN - #c.i.s.ComponentManagerImpl - com.intellij.psi.LanguageSubstitutors initializer requests com.intellij.psi.LanguageSubstitutors instance
2026-07-31 23:39:06,222 [   1549]   WARN - #c.i.s.ComponentManagerImpl - org.zmlx.hg4idea.provider.HgChangeProvider initializer requests com.intellij.openapi.vcs.FileStatusFactory instance
2026-07-31 23:39:06,519 [   1846] SEVERE - #c.i.c.ComponentStoreImpl - Cannot init component state (componentName=GradleJvmSupportMatrix, componentClass=GradleJvmSupportMatrix) [Plugin: com.intellij.gradle]
com.intellij.diagnostic.PluginException: Cannot init component state (componentName=GradleJvmSupportMatrix, componentClass=GradleJvmSupportMatrix) [Plugin: com.intellij.gradle]
	at com.intellij.configurationStore.ComponentStoreImpl.initComponent(ComponentStoreImpl.kt:174)
	at com.intellij.configurationStore.ComponentStoreWithExtraComponents.initComponent(ComponentStoreWithExtraComponents.kt:48)
	at com.intellij.serviceContainer.ComponentManagerImpl.initializeComponent$intellij_platform_serviceContainer(ComponentManagerImpl.kt:931)
	at com.intellij.serviceContainer.ServiceInstanceInitializer.createInstance$suspendImpl(ServiceInstanceInitializer.kt:41)
	at com.intellij.serviceContainer.ServiceInstanceInitializer.createInstance(ServiceInstanceInitializer.kt)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder$initialize$1$1.invokeSuspend(LazyInstanceHolder.kt:162)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder$initialize$1$1.invoke(LazyInstanceHolder.kt)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder$initialize$1$1.invoke(LazyInstanceHolder.kt)
	at kotlinx.coroutines.intrinsics.UndispatchedKt.startUndispatchedOrReturn(Undispatched.kt:78)
	at kotlinx.coroutines.BuildersKt__Builders_commonKt.withContext(Builders.common.kt:167)
	at kotlinx.coroutines.BuildersKt.withContext(Unknown Source)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder$initialize$1.invokeSuspend(LazyInstanceHolder.kt:160)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder$initialize$1.invoke(LazyInstanceHolder.kt)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder$initialize$1.invoke(LazyInstanceHolder.kt)
	at kotlinx.coroutines.intrinsics.UndispatchedKt.startCoroutineUndispatched(Undispatched.kt:44)
	at kotlinx.coroutines.CoroutineStart.invoke(CoroutineStart.kt:112)
	at kotlinx.coroutines.AbstractCoroutine.start(AbstractCoroutine.kt:126)
	at kotlinx.coroutines.BuildersKt__Builders_commonKt.launch(Builders.common.kt:56)
	at kotlinx.coroutines.BuildersKt.launch(Unknown Source)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder.initialize(LazyInstanceHolder.kt:145)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder.access$initialize(LazyInstanceHolder.kt:13)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder.tryInitialize(LazyInstanceHolder.kt:135)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder.getInstance(LazyInstanceHolder.kt:95)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder.getInstanceInCallerContext$suspendImpl(LazyInstanceHolder.kt:87)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder.getInstanceInCallerContext(LazyInstanceHolder.kt)
	at com.intellij.serviceContainer.ComponentManagerImplKt$getOrCreateInstanceBlocking$3.invokeSuspend(ComponentManagerImpl.kt:2337)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:108)
	at kotlinx.coroutines.EventLoopImplBase.processNextEvent(EventLoop.common.kt:280)
	at kotlinx.coroutines.BlockingCoroutine.joinBlocking(Builders.kt:85)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking(Builders.kt:59)
	at kotlinx.coroutines.BuildersKt.runBlocking(Unknown Source)
	at com.intellij.serviceContainer.ComponentManagerImplKt$runBlockingInitialization$1.invoke(ComponentManagerImpl.kt:2406)
	at com.intellij.serviceContainer.ComponentManagerImplKt$runBlockingInitialization$1.invoke(ComponentManagerImpl.kt:2397)
	at com.intellij.openapi.progress.ContextKt.prepareThreadContext(context.kt:83)
	at com.intellij.serviceContainer.ComponentManagerImplKt.runBlockingInitialization(ComponentManagerImpl.kt:2397)
	at com.intellij.serviceContainer.ComponentManagerImplKt.getOrCreateInstanceBlocking(ComponentManagerImpl.kt:2336)
	at com.intellij.serviceContainer.ComponentManagerImpl.doGetService(ComponentManagerImpl.kt:1057)
	at com.intellij.serviceContainer.ComponentManagerImpl.getService(ComponentManagerImpl.kt:988)
	at org.jetbrains.plugins.gradle.jvmcompat.GradleJvmSupportMatrix$Companion.getInstance(GradleJvmSupportMatrix.kt:220)
	at org.jetbrains.plugins.gradle.jvmcompat.GradleCompatibilitySupportUpdater.<init>(GradleCompatibilitySupportUpdater.kt:14)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:499)
	at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:480)
	at com.intellij.platform.instanceContainer.instantiation.InstantiateKt$instantiate$4.invoke(instantiate.kt:74)
	at com.intellij.platform.instanceContainer.instantiation.InstantiateKt$instantiate$4.invoke(instantiate.kt:72)
	at com.intellij.platform.instanceContainer.instantiation.InstantiateKt.instantiate(instantiate.kt:278)
	at com.intellij.platform.instanceContainer.instantiation.InstantiateKt.instantiate(instantiate.kt:72)
	at com.intellij.serviceContainer.InstantiateKt.instantiateWithContainer(instantiate.kt:19)
	at com.intellij.serviceContainer.ServiceInstanceInitializer.createInstance$suspendImpl(ServiceInstanceInitializer.kt:26)
	at com.intellij.serviceContainer.ServiceInstanceInitializer.createInstance(ServiceInstanceInitializer.kt)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder$initialize$1$1.invokeSuspend(LazyInstanceHolder.kt:162)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder$initialize$1$1.invoke(LazyInstanceHolder.kt)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder$initialize$1$1.invoke(LazyInstanceHolder.kt)
	at kotlinx.coroutines.intrinsics.UndispatchedKt.startUndispatchedOrReturn(Undispatched.kt:78)
	at kotlinx.coroutines.BuildersKt__Builders_commonKt.withContext(Builders.common.kt:167)
	at kotlinx.coroutines.BuildersKt.withContext(Unknown Source)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder$initialize$1.invokeSuspend(LazyInstanceHolder.kt:160)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder$initialize$1.invoke(LazyInstanceHolder.kt)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder$initialize$1.invoke(LazyInstanceHolder.kt)
	at kotlinx.coroutines.intrinsics.UndispatchedKt.startCoroutineUndispatched(Undispatched.kt:44)
	at kotlinx.coroutines.CoroutineStart.invoke(CoroutineStart.kt:112)
	at kotlinx.coroutines.AbstractCoroutine.start(AbstractCoroutine.kt:126)
	at kotlinx.coroutines.BuildersKt__Builders_commonKt.launch(Builders.common.kt:56)
	at kotlinx.coroutines.BuildersKt.launch(Unknown Source)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder.initialize(LazyInstanceHolder.kt:145)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder.access$initialize(LazyInstanceHolder.kt:13)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder.tryInitialize(LazyInstanceHolder.kt:135)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder.getInstance(LazyInstanceHolder.kt:95)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder.getInstanceInCallerContext$suspendImpl(LazyInstanceHolder.kt:87)
	at com.intellij.platform.instanceContainer.internal.LazyInstanceHolder.getInstanceInCallerContext(LazyInstanceHolder.kt)
	at com.intellij.serviceContainer.ComponentManagerImplKt$getOrCreateInstanceBlocking$3.invokeSuspend(ComponentManagerImpl.kt:2337)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:108)
	at kotlinx.coroutines.EventLoopImplBase.processNextEvent(EventLoop.common.kt:280)
	at kotlinx.coroutines.BlockingCoroutine.joinBlocking(Builders.kt:85)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking(Builders.kt:59)
	at kotlinx.coroutines.BuildersKt.runBlocking(Unknown Source)
	at com.intellij.serviceContainer.ComponentManagerImplKt$runBlockingInitialization$1.invoke(ComponentManagerImpl.kt:2406)
	at com.intellij.serviceContainer.ComponentManagerImplKt$runBlockingInitialization$1.invoke(ComponentManagerImpl.kt:2397)
	at com.intellij.openapi.progress.ContextKt.prepareThreadContext(context.kt:86)
	at com.intellij.serviceContainer.ComponentManagerImplKt.runBlockingInitialization(ComponentManagerImpl.kt:2397)
	at com.intellij.serviceContainer.ComponentManagerImplKt.getOrCreateInstanceBlocking(ComponentManagerImpl.kt:2336)
	at com.intellij.serviceContainer.ComponentManagerImpl.doGetService(ComponentManagerImpl.kt:1057)
	at com.intellij.serviceContainer.ComponentManagerImpl.getService(ComponentManagerImpl.kt:988)
	at org.jetbrains.plugins.gradle.jvmcompat.GradleCompatibilitySupportUpdater$Companion.getInstance(GradleCompatibilitySupportUpdater.kt:26)
	at org.jetbrains.plugins.gradle.service.project.GradleVersionUpdateStartupActivity.execute(GradleVersionUpdateStartupActivity.kt:12)
	at com.intellij.ide.startup.impl.StartupManagerImplKt$launchActivity$1.invokeSuspend(StartupManagerImpl.kt:482)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:108)
	at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:584)
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:793)
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:697)
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:684)
Caused by: java.lang.IllegalArgumentException: 25
	at com.intellij.util.lang.JavaVersion.parse(JavaVersion.java:307)
	at org.jetbrains.plugins.gradle.jvmcompat.GradleJvmSupportMatrix$getCompatibilityRanges$1$javaRange$1.invoke(GradleJvmSupportMatrix.kt:44)
	at org.jetbrains.plugins.gradle.jvmcompat.GradleJvmSupportMatrix$getCompatibilityRanges$1$javaRange$1.invoke(GradleJvmSupportMatrix.kt:44)
	at org.jetbrains.plugins.gradle.jvmcompat.IdeVersionedDataParser$Companion.parseVersion(IdeVersionedDataParser.kt:21)
	at org.jetbrains.plugins.gradle.jvmcompat.IdeVersionedDataParser$Companion.parseRange(IdeVersionedDataParser.kt:31)
	at org.jetbrains.plugins.gradle.jvmcompat.GradleJvmSupportMatrix.getCompatibilityRanges(GradleJvmSupportMatrix.kt:44)
	at org.jetbrains.plugins.gradle.jvmcompat.GradleJvmSupportMatrix.applyState(GradleJvmSupportMatrix.kt:28)
	at org.jetbrains.plugins.gradle.jvmcompat.GradleJvmSupportMatrix.onStateChanged(GradleJvmSupportMatrix.kt:50)
	at org.jetbrains.plugins.gradle.jvmcompat.GradleJvmSupportMatrix.onStateChanged(GradleJvmSupportMatrix.kt:13)
	at org.jetbrains.plugins.gradle.jvmcompat.IdeVersionedDataStorage.loadState(IdeVersionedDataStorage.kt:38)
	at org.jetbrains.plugins.gradle.jvmcompat.IdeVersionedDataStorage.loadState(IdeVersionedDataStorage.kt:7)
	at com.intellij.configurationStore.ComponentStoreImpl.doInitComponent(ComponentStoreImpl.kt:490)
	at com.intellij.configurationStore.ComponentStoreImpl.initComponent(ComponentStoreImpl.kt:409)
	at com.intellij.configurationStore.ComponentStoreImpl.initComponent(ComponentStoreImpl.kt:131)
	... 95 more
2026-07-31 23:39:06,554 [   1881] SEVERE - #c.i.c.ComponentStoreImpl - IntelliJ IDEA 2023.3  Build #IC-233.11799.241
2026-07-31 23:39:06,554 [   1881] SEVERE - #c.i.c.ComponentStoreImpl - JDK: 17.0.9; VM: OpenJDK 64-Bit Server VM; Vendor: JetBrains s.r.o.
2026-07-31 23:39:06,554 [   1881] SEVERE - #c.i.c.ComponentStoreImpl - OS: Mac OS X
2026-07-31 23:39:06,555 [   1882] SEVERE - #c.i.c.ComponentStoreImpl - Last Action: 
2026-07-31 23:39:06,619 [   1946]   WARN - #c.i.i.s.i.StartupManagerImpl - Migrate com.c301.plugin.config.PluginOnboardingStartupActivity to ProjectActivity [Plugin: commit-template-plugin]
com.intellij.diagnostic.PluginException: Migrate com.c301.plugin.config.PluginOnboardingStartupActivity to ProjectActivity [Plugin: commit-template-plugin]
	at com.intellij.ide.startup.impl.StartupManagerImpl.runPostStartupActivities(StartupManagerImpl.kt:277)
	at com.intellij.ide.startup.impl.StartupManagerImpl.access$runPostStartupActivities(StartupManagerImpl.kt:69)
	at com.intellij.ide.startup.impl.StartupManagerImpl$runPostStartupActivities$3$2.invokeSuspend(StartupManagerImpl.kt:192)
	at com.intellij.ide.startup.impl.StartupManagerImpl$runPostStartupActivities$3$2.invoke(StartupManagerImpl.kt)
	at com.intellij.ide.startup.impl.StartupManagerImpl$runPostStartupActivities$3$2.invoke(StartupManagerImpl.kt)
	at kotlinx.coroutines.intrinsics.UndispatchedKt.startUndispatchedOrReturn(Undispatched.kt:78)
	at kotlinx.coroutines.BuildersKt__Builders_commonKt.withContext(Builders.common.kt:167)
	at kotlinx.coroutines.BuildersKt.withContext(Unknown Source)
	at com.intellij.ide.startup.impl.StartupManagerImpl$runPostStartupActivities$3.invokeSuspend(StartupManagerImpl.kt:191)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:108)
	at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:584)
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:793)
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:697)
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:684)
2026-07-31 23:39:06,980 [   2307]   WARN - #o.j.p.t.TextMateService - Cannot find referenced file `./language-configuration.json` in bundle `/Users/muzi/Documents/Code_Class/Maven_Class/repository/caches/modules-2/files-2.1/com.jetbrains.intellij.idea/ideaIC/2023.3/6105b81c6142f62379ad6c5afb542c77350a71eb/ideaIC-2023.3/plugins/textmate/lib/bundles/mdx`
2026-07-31 23:39:07,662 [   2989]   WARN - #c.i.o.a.i.ActionUpdater - 651 ms to grab EDT for ToolWindowHeader$2#Update@ToolwindowTitle (com.intellij.toolWindow.ToolWindowHeader$2)
2026-07-31 23:39:08,305 [   3632]   WARN - #c.i.u.x.Binding - no accessors for org.jetbrains.idea.maven.project.MavenHomeType
2026-07-31 23:39:09.128 java[35071:4211581] error messaging the mach port for IMKCFRunLoopWakeUpReliable
2026-07-31 23:39:09,913 [   5240]   WARN - #c.i.d.IdeErrorsDialog - com.intellij.util.io.HttpRequests$HttpStatusException: Request failed with status code 404. Status=404, Url=https://ea-report.jetbrains.com/developer/list
2026-07-31 23:39:14,743 [  10070]   WARN - #c.i.o.o.e.ConfigurableExtensionPointUtil - ignore deprecated groupId: language for id: preferences.language.Kotlin.scripting
2026-07-31 23:39:37,374 [  32701]   WARN - #c.i.o.o.e.ConfigurableExtensionPointUtil - ignore deprecated groupId: language for id: preferences.language.Kotlin.scripting
