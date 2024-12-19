package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.actions.AbstractToggleUseSoftWrapsAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/12/19
 */
public class ToggleUseSoftWrapsAction extends AbstractToggleUseSoftWrapsAction implements UpdateInBackground {

    private final EditorEx editor;
    private final SimpleToolWindowPanel simpleToolWindowPanel;

    /**
     * Creates new {@code AbstractToggleUseSoftWrapsAction} object.
     *
     * @param appliancePlace defines type of the place where soft wraps are applied
     * @param global         indicates if soft wraps should be changed for the current editor only or for the all editors
     *                       used at the target appliance place
     */
    public ToggleUseSoftWrapsAction(EditorEx editor, SimpleToolWindowPanel simpleToolWindowPanel) {
        super(SoftWrapAppliancePlaces.MAIN_EDITOR, false);
        this.editor = editor;
        this.simpleToolWindowPanel = simpleToolWindowPanel;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.toggle.softWraps.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.toggle.softWraps.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.SOFT_WRAP);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(
                getEventProject(e) != null
                        && StrUtil.isNotBlank(editor.getDocument().getText())
                        && JsonAssistantToolWindowPanel.isEditorCardDisplayed(simpleToolWindowPanel));
    }
}
