package cn.memoryzy.json.extension.editor;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @author Memory
 * @since 2025/7/7
 */
public class ImportHistoryFloatingToolbarProvider implements FloatingToolbarProvider, Disposable {

    // TODO FloatingToolbarProvider 的实现类不能被混淆，因为没有标注@Override，所以这里不混淆

    public int getPriority() {
        return 0;
    }

    public boolean getAutoHideable() {
        return false;
    }

    public @NotNull ActionGroup getActionGroup() {
        return (ActionGroup) ActionHolder.HISTORY_FLOAT_GROUP;
    }


    public void register(@NotNull DataContext dataContext, @NotNull FloatingToolbarComponent component, @NotNull Disposable disposable) {
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null || editor.isDisposed()) {
            component.scheduleHide();
            return;
        }

        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null || project.isDisposed()) {
            component.scheduleHide();
            return;
        }

        // 是否为插件自定义的编辑器
        String userData = editor.getUserData(HistoryToolWindowComponentProvider.HISTORY_EDITOR_FLAG);
        if (StrUtil.isBlank(userData)) {
            component.scheduleHide();
            return;
        }

        // 默认都予展示
        component.scheduleShow();
    }

    /**
     * 兼容203版本
     */
    public void register(@NotNull FloatingToolbarComponent component, @NotNull Disposable disposable) {
        DataManager manager = DataManager.getInstance();
        DataContext dataContext = manager.getDataContext((Component) component);
        register(dataContext, component, disposable);
    }

    public void dispose() {

    }
}
