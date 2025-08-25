package cn.memoryzy.json.extension.widget;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/8/25
 */
public class CountCharWidgetFactory extends StatusBarEditorBasedWidgetFactory implements DumbAware {

    @Override
    public @NonNls @NotNull String getId() {
        return CountCharStatusBarWidget.ID;
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return JsonAssistantBundle.messageOnSystem("widget.count.char.name");
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new CountCharStatusBarWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        Disposer.dispose(widget);
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }

}
