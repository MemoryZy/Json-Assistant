package cn.memoryzy.json.ui.extension;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import com.intellij.icons.AllIcons;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.scale.JBUIScale;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class SearchExtension implements ExtendableTextComponent.Extension {
    private final Runnable runnable;

    public SearchExtension(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public Icon getIcon(boolean hovered) {
        return AllIcons.Actions.Search;
    }

    @Override
    public String getTooltip() {
        return JsonAssistantBundle.messageOnSystem("search.extension.tooltip");
    }

    @Override
    public int getIconGap() {
        return JBUIScale.scale(2);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Runnable getActionOnClick() {
        return runnable;
    }

}