package cn.memoryzy.json.ui.editor;

import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.scale.JBUIScale;
import icons.JsonAssistantIcons;

import javax.swing.*;

/**
 * @author Memory
 * @since 2025/8/21
 */
public class EditExtension implements ExtendableTextComponent.Extension {

    @Override
    public Icon getIcon(boolean hovered) {
        return JsonAssistantIcons.ToolWindow.EDIT;
    }

    @Override
    public int getAfterIconOffset() {
        return JBUIScale.scale(6);
    }

    @Override
    public int getIconGap() {
        return JBUIScale.scale(2);
    }

    @Override
    public boolean isIconBeforeText() {
        return true;
    }

    @Override
    public String toString() {
        return "edit";
    }

}
