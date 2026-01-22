package cn.memoryzy.json.action.toolwindow.history;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ToolWindowConstant;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.AbstractToggleUseSoftWrapsAction;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Memory
 * @since 2025/11/21
 */
public class ToggleUseSoftWrapsHistoryAction extends AbstractToggleUseSoftWrapsAction implements UpdateInBackground {

    private final HistoryToolWindowComponentProvider provider;

    public ToggleUseSoftWrapsHistoryAction(HistoryToolWindowComponentProvider provider) {
        super(SoftWrapAppliancePlaces.MAIN_EDITOR, false);
        this.provider = provider;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.toggle.softWraps.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.toggle.softWraps.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.SOFT_WRAP);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        super.setSelected(e, state);
        // 为其余编辑器也设置软换行
        List<Editor> restEditors = provider.getRestEditors();
        for (Editor restEditor : restEditors) {
            // 设置/取消软换行
            AbstractToggleUseSoftWrapsAction.toggleSoftWraps(restEditor, null, state);
        }

        // 存储状态
        saveToPropertiesComponent(state);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Project project = getEventProject(e);
        Editor activeEditor = provider.getActiveEditor();
        boolean enabled = null != project
                && null != activeEditor
                && StrUtil.isNotBlank(activeEditor.getDocument().getText());

        if (enabled) {
            boolean selected = isSelected(e);
            Toggleable.setSelected(presentation, selected);
        }

        presentation.setEnabled(enabled);
    }

    @Override
    protected @Nullable Editor getEditor(@NotNull AnActionEvent e) {
        return provider.getActiveEditor();
    }

    private void saveToPropertiesComponent(boolean state) {
        PropertiesComponent.getInstance().setValue(ToolWindowConstant.History.SOFT_WRAPS_HISTORY_SELECT_STATE, state + "");
    }
}
