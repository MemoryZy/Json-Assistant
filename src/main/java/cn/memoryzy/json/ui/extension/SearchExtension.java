package cn.memoryzy.json.ui.extension;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.scale.JBUIScale;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class SearchExtension implements ExtendableTextComponent.Extension {

    @Override
    public Icon getIcon(boolean hovered) {
        return AllIcons.Actions.Search;
    }

    @Override
    public String getTooltip() {
        return null;
    }

    @Override
    public int getIconGap() {
        return JBUIScale.scale(2);
    }

    @Override
    public boolean isIconBeforeText() {
        return true;
    }
}
