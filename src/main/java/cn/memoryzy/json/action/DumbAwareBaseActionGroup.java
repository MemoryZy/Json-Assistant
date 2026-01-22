package cn.memoryzy.json.action;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2025/9/18
 */
public abstract class DumbAwareBaseActionGroup extends DefaultActionGroup implements DumbAware, UpdateInBackground {

    public DumbAwareBaseActionGroup() {
    }

    public DumbAwareBaseActionGroup(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super();
        Presentation presentation = getTemplatePresentation();
        presentation.setText(text);
        presentation.setDescription(description);
        presentation.setIcon(icon);
    }

}
