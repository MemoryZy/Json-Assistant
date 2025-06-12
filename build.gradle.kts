import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.tasks.RunPluginVerifierTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
        classpath("com.guardsquare:proguard-gradle:7.7.0")
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
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it.get()
            targetCompatibility = it.get()
            options.encoding = "UTF-8"
        }
    }

    // 注册一个名为 "proguard" 的 Gradle 任务
    register<proguard.gradle.ProGuardTask>("proguard") {
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
        printmapping("build/obfuscated/output/${properties("pluginName").get()}-${properties("pluginVersion").get()}-ProGuard-ChangeLog.txt")

        // 指定目标插件版本（使用插件版本避免字节码版本问题）
        target(properties("javaVersion").get())

        // 混淆资源文件名（与类名同步修改，这里可能会混淆图标）
        adaptresourcefilenames()
        // 设置优化次数为9次（虽然禁用优化，但保留此配置以防启用）
        optimizationpasses(9)
        // 允许修改访问修饰符（增强混淆效果）
        allowaccessmodification()

        // 保留指定的重要类属性（如注解、行号表等调试/反射必需信息）
        keepattributes("Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod")

        // 保留实现特定接口的类（确保插件持久化状态组件不被混淆）
        keep("""
            class * implements com.intellij.openapi.components.PersistentStateComponent {*;}
             """.trimIndent()
        )

        // TODO 目前还是存在图标丢失的问题

        keepdirectories("icons")
        keepdirectories("icons/**")

        keep("class cn.memoryzy.json.service.persistent.state.** { *; }")

        // 保留类的静态实例成员（单例模式保护）
        keepclassmembers("""
            class * {public static ** INSTANCE;}
             """.trimIndent()
        )

        // 保留整个工具类（com.intellij.util包下所有类）
        keep("class com.intellij.util.* {*;}")
    }

    // 配置准备沙箱任务（打包插件前的步骤）
    prepareSandbox {
        // 检查是否启用了混淆（通过 enableProGuard 属性控制）
        if (properties("enableProGuard").map(String::toBoolean).getOrElse(false)) {
            // 使沙箱任务依赖 proguard 任务
            dependsOn("proguard")
            // 使用混淆后的 JAR 作为插件主文件
            pluginJar.set(File("build/obfuscated/output/instrumented-${properties("pluginName").get()}-${properties("pluginVersion").get()}.jar"))
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

