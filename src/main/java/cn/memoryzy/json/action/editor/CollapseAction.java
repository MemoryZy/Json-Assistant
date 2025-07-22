package cn.memoryzy.json.action.editor;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/7/21
 */
public class CollapseAction extends DumbAwareAction implements UpdateInBackground {

    public CollapseAction(Editor editor) {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText("Collapse");
        presentation.setIcon(AllIcons.General.CollapseComponent);
        presentation.setHoveredIcon(AllIcons.General.CollapseComponentHover);
        registerCustomShortcutSet(CustomShortcutSet.fromString("shift ENTER"), editor.getContentComponent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
