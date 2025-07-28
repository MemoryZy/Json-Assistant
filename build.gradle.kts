import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.tasks.RunPluginVerifierTask

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

buildscript {
    repositories {
        maven {
            setUrl("https://maven.aliyun.com/repository/public/")
            setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
            setUrl("https://plugins.gradle.org/m2/")
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.3.2")
    }
}

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.qodana) // Gradle Qodana Plugin
    alias(libs.plugins.kover) // Gradle Kover Plugin
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Configure project's dependencies
repositories {
    maven {
        setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
        setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    mavenCentral()
    gradlePluginPortal()
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
//    implementation(libs.annotations)
    implementation(libs.hutool)
    implementation(libs.dataformat)
    implementation(libs.jsonpath)
    implementation(libs.commonlang)
    implementation(libs.toml4j)
//    implementation(libs.tnjson)
    implementation(libs.jmespath)
    implementation(libs.woodstox)
//    implementation(libs.bytebuddy)
}

// Set the JVM language level used to build the project. Use Java 11 for 2020.3+, and Java 17 for 2022.2+.
kotlin {
    jvmToolchain(11)
}

// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")

    downloadSources = properties("platformDownloadSources").map(String::toBoolean).getOrElse(true)

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    version.set(properties("pluginVersion"))
    repositoryUrl = properties("pluginRepositoryUrl")
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath = provider { file(".qodana").canonicalPath }
    reportPath = provider { file("build/reports/inspections").canonicalPath }
    saveReport = true
    showReport = environment("QODANA_SHOW_REPORT").map { it.toBoolean() }.getOrElse(false)
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
kover.xmlReport {
    onCheck = true
}

tasks {
    runIde {
        systemProperty("idea.is.internal", true)
        autoReloadPlugins.set(true)

        // Enable hotswap, requires JBR 17+ or JBR 11 with DCEVM, and run in debug mode.
        jvmArgs(listOf("-XX:+AllowEnhancedClassRedefinition"))
    }

    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = properties("javaVersion").get()
        targetCompatibility = properties("javaVersion").get()
        options.encoding = "UTF-8"
    }

    // 注册一个名为 "proguard" 的 Gradle 任务
    register<proguard.gradle.ProGuardTask>("proguard") {
        // 告知 Gradle 每次都应该重新执行 (不加这个的话，每次启动时，Gradle 总是判断 ProGuard 无需变更，于是复用上一次的配置)
        outputs.upToDateWhen { false }

        // 声明此任务依赖 instrumentedJar 任务，确保先构建原始 JAR
        dependsOn(instrumentedJar)
        // 启用详细日志输出
        verbose()

        // 获取当前 Java 安装路径
        val javaHome = System.getProperty("java.home")
        // 将 Java 标准库的所有 jmods 文件添加为库引用（防止混淆系统类）
        File("$javaHome/jmods/").listFiles()!!.forEach { libraryjars(it.absolutePath)}

        // Use the jar task output as a input jar. This will automatically add the necessary task dependency.
        // 指定输入 JAR 文件路径（instrumentedJar 任务的输出）
        injars("build/libs/instrumented-${properties("pluginName").get()}-${properties("pluginVersion").get()}.jar")
        // 指定混淆后的输出 JAR 路径
        outjars("build/obfuscated/output/instrumented-${properties("pluginName").get()}-${properties("pluginVersion").get()}.jar")

        // 添加编译类路径的所有依赖库（防止混淆第三方库）
        libraryjars(configurations.compileClasspath.get())

        // 禁用代码缩减（不删除未使用的类/方法）
        dontshrink()
        // 禁用代码优化（保持字节码结构不变）
        dontoptimize()

        // ProGuard 在混淆处理 XML 时，会用GBK编码，这里需要在 gradle.properties 中的 org.gradle.jvmargs 指定 -Dfile.encoding=UTF-8

        // 自动调整字符串常量（如 XML 文件中的类名引用）
        adaptclassstrings("**.xml")
        // 自动调整资源文件内容（如 XML 中的类名）
        adaptresourcefilecontents("**.xml")

        // Allow methods with the same signature, except for the return type,
        // to get the same obfuscation name.
        // 对返回类型不同的重载方法使用相同混淆名（增强混淆强度）
        overloadaggressively()

        // Put all obfuscated classes into the nameless root package.
        // 将所有混淆后的类移至根包（即默认包，增加反编译难度）
        repackageclasses("")
        // 禁止显示所有警告（避免因警告中断构建）
        dontwarn()

        // 生成混淆前后类名/方法名的映射文件（用于调试）
        printmapping("build/obfuscated/output/${properties("pluginName").get()}-${properties("pluginVersion").get()}-ProGuard-Mapping.txt")

        // 指定目标插件版本（使用插件版本避免字节码版本问题）
        target(properties("pluginVersion").get())

        // 混淆资源文件名（与类名同步修改，这里可能会混淆图标）
        adaptresourcefilenames()
        // 设置优化次数为9次（虽然禁用优化，但保留此配置以防启用）
        optimizationpasses(9)
        // 允许修改访问修饰符（增强混淆效果）
        allowaccessmodification()

        // 开启这个之后，在堆栈中不会显示具体的原类名 [at cU.a(JsonAssistantToolWindowComponentProvider:300)]，只会显示 [at cU.a(SourceFile:300)]
        renamesourcefileattribute("SourceFile")

        // ------------------------------------------- 规则

        // 保留指定的重要类属性（如注解、行号表等调试/反射必需信息）
        keepattributes("Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod")

        // 保留所有字段名及所有Getter/Setter（用于状态序列化），
        keepclassmembers("""
            class * implements com.intellij.openapi.components.PersistentStateComponent {
                <fields>;
                *** get*();
                void set*(***);
                *** set*(***);
                boolean is*();
                void load*(***);
            }
        """.trimIndent())

        // State 存储对象的字段不能被混淆，不然它们在xml中的key就会变成 a b c 这样的，并且每次都不同
        keepclassmembers("""
            class cn.memoryzy.json.service.persistent.state.** {
                <fields>;
                *** get*();
                void set*(***);
                *** set*(***);
                boolean is*();
            }
        """.trimIndent())

        // 涉及到反序列化的类也不能被混淆
        keepclassmembers("""
            class cn.memoryzy.json.model.deserializer.** {
                <fields>;
                *** get*();
                void set*(***);
                *** set*(***);
                boolean is*();
            }
        """.trimIndent())

        // 保留类的静态实例成员（单例模式保护）
        keepclassmembers("""
            class * {public static ** INSTANCE;}
             """.trimIndent()
        )

        // 保留整个工具类（com.intellij.util包下所有类）
        keep("class com.intellij.util.* {*;}")

        // 因为在默认打包的时候，java/icons 和 resources/icons 两个目录会被打到一起，
        // 所以需要过滤包及包下的类名不被混淆，但是方法、字段等可以被混淆
        keep("class icons.*")

        // Inspection 和 Intention 不能混淆，因为要关联 resources 目录下的描述
        keepnames("class * extends com.intellij.codeInspection.LocalInspectionTool")
        keepnames("class * implements com.intellij.codeInsight.intention.IntentionAction")

        // 不混淆枚举类，因为 JSON 反序列化时会根据枚举常量名来进行，如果混淆了这个，就会出现找不到的问题
        keepclassmembers("enum * {*;}")

        // 不开启这个的话，fileTemplates 文件模板无法找到
        keepdirectories()

        // 反射调用相关的方法、字段，也不能被混淆 (例如 BlacklistEntry.toJson()，序列化时，JSON5处理器默认会调用此方法)
        keepclassmembers("class * implements com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider {<methods>;}")


    }

    // 配置准备沙箱任务（打包插件前的步骤）
    prepareSandbox {
        // 当前环境是否为 ci 环境，在此环境下，无需混淆 (优先级大于本地 enableProGuard 属性)
        val isCiMode = environment("CI_MODE").map(String::toBoolean).getOrElse(false)
        // 检查本地是否启用了混淆（通过 enableProGuard 属性控制）
        val useProGuard = properties("enableProGuard").map(String::toBoolean).getOrElse(false)
        // 是否为发布环境
        val isReleaseMode = environment("RELEASE_MODE").map(String::toBoolean).getOrElse(false)

        // 只有不处于 ci 环境中，且启用了混淆才开始执行
        if (isReleaseMode || (!isCiMode && useProGuard)) {
            // 使沙箱任务依赖 proguard 任务
            dependsOn("proguard")
            // 使用混淆后的 JAR 作为插件主文件
            pluginJar.set(File("build/obfuscated/output/instrumented-${properties("pluginName").get()}-${properties("pluginVersion").get()}.jar"))
        } else {
            logger.lifecycle("Obfuscation has been mandatorily disabled in CI mode.")
        }
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")

        pluginDescription = projectDir.resolve("DESCRIPTION.md").readText().let (::markdownToHTML)

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    runPluginVerifier {
        // 验证问题类型
        failureLevel.set(listOf(
            RunPluginVerifierTask.FailureLevel.COMPATIBILITY_PROBLEMS,
            RunPluginVerifierTask.FailureLevel.INTERNAL_API_USAGES,
            RunPluginVerifierTask.FailureLevel.INVALID_PLUGIN,
            RunPluginVerifierTask.FailureLevel.MISSING_DEPENDENCIES,
            RunPluginVerifierTask.FailureLevel.OVERRIDE_ONLY_API_USAGES
        ))

        // 验证平台列表
        ideVersions.set(properties("pluginVerifierVersions").map { it.split(',').map(String::trim).filter(String::isNotEmpty) })

        // 验证结果输出格式 -> https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-runpluginverifier
        verificationReportsFormats.set(listOf(
            RunPluginVerifierTask.VerificationReportsFormats.PLAIN,
            RunPluginVerifierTask.VerificationReportsFormats.HTML,
            RunPluginVerifierTask.VerificationReportsFormats.MARKDOWN,
        ))

        teamCityOutputFormat.set(true)
    }

    signPlugin {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map { listOf(it.split('-').getOrElse(1) { "default" }.split('.').first()) }
    }
}

