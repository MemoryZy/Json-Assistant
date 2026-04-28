package cn.memoryzy.json.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.util.ReflectUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.action.test.BaseTestDumbAwareAction;
import cn.memoryzy.json.util.FontManager;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.ide.AppLifecycleListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.project.ProjectType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Memory
 * @since 2025/6/4
 */
public class AppLifecycleManager implements AppLifecycleListener {

    private static final Logger LOG = Logger.getInstance(AppLifecycleManager.class);

    @Override
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        // 合并旧配置
        ConfigurationMerger.getInstance().mergeGlobalLegacySettings();
        // 初始化字体文件
        FontManager.loadAndDownloadJetbrainsMapleMonoFont();

        // ------------------------------------- 调试
        loadAllTestAction();
    }


    private void loadAllTestAction() {
        // 只在测试环境执行
        if (!PlatformUtil.isTestEnvironment()) return;

        // 如果是测试环境，则加载 /test 包下的所有 Action
        ClassScanner classScanner = new ClassScanner(
                "cn.memoryzy.json.action.test",
                clz -> AnAction.class.isAssignableFrom(clz) && !clz.isInterface() && !Modifier.isAbstract(clz.getModifiers()));
        classScanner.setClassLoader(this.getClass().getClassLoader());
        Set<Class<?>> classes = classScanner.scan();
        if (CollUtil.isEmpty(classes)) return;

        try {
            ActionManager actionManager = ActionManager.getInstance();
            IdeaPluginDescriptor plugin = JsonAssistantPlugin.getJsonAssistantPlugin();

            for (Class<?> clz : classes) {
                // 必须要有无参构造器
                BaseTestDumbAwareAction action = (BaseTestDumbAwareAction) ReflectUtil.newInstance(clz);
                String actionClass = clz.getName();
                String actionId = action.getActionId();
                String iconPath = action.getIconPath();
                String actionText = action.getActionText();
                String parentGroupId = action.getParentGroupId();
                Constraints constraint = action.getConstraints();

                // 通过反射构建
                ActionStub actionStub = (ActionStub) createInstanceIfConstructorMatches(
                        "com.intellij.openapi.actionSystem.ActionStub",
                        actionClass,
                        actionId,
                        Objects.requireNonNull(plugin),
                        iconPath,
                        null,
                        () -> {
                            Presentation presentation = new Presentation();
                            presentation.setText(actionText);
                            return presentation;
                        });

                if (null == actionStub) continue;

                // 注册
                actionManager.registerAction(actionId, action);

                // 查找父组
                String actionName = actionStub.getClassName() + " (" + actionStub.getId() + ")";
                Object[] parameters1 = {parentGroupId, actionName, plugin};
                DefaultActionGroup parentGroup = ReflectUtil.invoke(actionManager, "getParentGroup", parameters1);

                // 添加指定Action
                Object[] parameters2 = {parentGroup, actionStub, constraint, plugin, false};
                ReflectUtil.invoke(actionManager, "addToGroupInner", parameters2);
            }
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    public static Object createInstanceIfConstructorMatches(String className,
                                                            String actionClass,
                                                            String id,
                                                            PluginDescriptor plugin,
                                                            String iconPath,
                                                            ProjectType projectType,
                                                            Supplier<Presentation> templatePresentation) {

        try {
            // 1. 获取类的Class对象
            Class<?> clazz = Class.forName(className);

            // 2. 定义目标构造器的参数类型数组
            // 关键：参数类型顺序必须与构造器声明完全一致
            Class<?>[] expectedParameterTypes = new Class<?>[]{
                    String.class,
                    String.class,
                    PluginDescriptor.class,
                    String.class,
                    ProjectType.class,
                    Supplier.class // 注意：泛型在运行时会被擦除，使用原始Supplier类型
            };

            // 3. 尝试获取精确匹配的构造器
            Constructor<?> constructor = ReflectUtil.getConstructor(clazz, expectedParameterTypes);
            if (null == constructor) return null;

            // 4. 传入实际参数创建实例
            return constructor.newInstance(
                    actionClass,
                    id,
                    plugin,
                    iconPath,
                    projectType,
                    templatePresentation);

        } catch (Exception e) {
            return null;
        }
    }

}
