package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ToolWindowConstant;
import cn.memoryzy.json.ui.panel.CombineCardLayout;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.AbstractToggleUseSoftWrapsAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/12/19
 */
public class ToggleUseSoftWrapsAction extends AbstractToggleUseSoftWrapsAction implements UpdateInBackground {

    private final EditorEx editor;
    private final CombineCardLayout cardLayout;

    public ToggleUseSoftWrapsAction(EditorEx editor, CombineCardLayout cardLayout) {
        super(SoftWrapAppliancePlaces.MAIN_EDITOR, false);
        this.editor = editor;
        this.cardLayout = cardLayout;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.toggle.softWraps.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.toggle.softWraps.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.SOFT_WRAP);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        super.setSelected(e, state);
        // 存储状态
        saveToPropertiesComponent(state);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = getEventProject(e) != null
                && StrUtil.isNotBlank(editor.getDocument().getText())
                && cardLayout.isEditorView();

        final Presentation presentation = e.getPresentation();
        if (enabled) {
            boolean selected = isSelected(e);
            Toggleable.setSelected(presentation, selected);
        }

        presentation.setEnabled(enabled);
    }

    @Override
    protected @Nullable Editor getEditor(@NotNull AnActionEvent e) {
        return editor;
    }

    private void saveToPropertiesComponent(boolean state) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.setValue(ToolWindowConstant.Main.SOFT_WRAPS_SELECT_STATE, state + "");
    }
}
