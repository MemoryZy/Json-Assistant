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
public class ExpandAction extends DumbAwareAction implements UpdateInBackground {

    public ExpandAction(Editor editor) {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText("Expand");
        presentation.setIcon(AllIcons.General.ExpandComponent);
        presentation.setHoveredIcon(AllIcons.General.ExpandComponentHover);
        registerCustomShortcutSet(CustomShortcutSet.fromString("shift ENTER"), editor.getContentComponent());
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
