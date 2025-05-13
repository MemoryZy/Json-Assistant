package cn.memoryzy.json.extension;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.actionSystem.AnAction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2025/3/23
 */
public class ProxyInjector implements AppLifecycleListener {

    @Override
    @SuppressWarnings("StatementWithEmptyBody")
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        if (canInjectProxy()) {
            // 当有需要时，可以将操作改为动态注册，注入代理类
        }
    }


    private boolean canInjectProxy() {
        Class<?> clz = JsonAssistantUtil.getClassByName("com.intellij.openapi.actionSystem.ActionUpdateThread");
        Method method = ReflectUtil.getMethodByName(AnAction.class, "getActionUpdateThread");
        return ClassUtil.isEnum(clz) && Objects.nonNull(method);
    }

    private boolean isByteBuddyGenerated(Class<?> clz) {
        return clz.getName().contains("$ByteBuddy$");
    }


    // private Class<?> generateProxyClassWithMethod(Class<?> clz, String methodName, Object returnValue) {
    //     try (DynamicType.Unloaded<?> dynamicType = new ByteBuddy()
    //             .subclass(clz)
    //             .defineMethod(methodName, returnValue.getClass(), Visibility.PUBLIC)
    //             .intercept(FixedValue.value(returnValue))
    //             .make()) {
    //
    //         return dynamicType
    //                 .load(ClassLoader.getSystemClassLoader())
    //                 .getLoaded();
    //
    //     } catch (Exception e) {
    //         return null;
    //     }
    // }

}
