package cn.memoryzy.json.extension;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.PlatformUtil;
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
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        if (canInjectProxy()) {
            PlatformUtil.setClipboard("can --------------");
        }
    }


    private boolean canInjectProxy() {
        Class<?> clz = JsonAssistantUtil.getClassByName("com.intellij.openapi.actionSystem.ActionUpdateThread");
        Method method = ReflectUtil.getMethodByName(AnAction.class, "getActionUpdateThread");
        return ClassUtil.isEnum(clz) && Objects.nonNull(method);
    }

}
