package cn.memoryzy.json.action.query;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.IdeFocusManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Memory
 * @since 2024/8/21
 */
public class ShowHistoryAction extends DumbAwareAction implements UpdateInBackground {

    private final Component searchWrapper;
    private final JComponent searchTextField;
    private final Supplier<String> propertyNameSupplier;

    public ShowHistoryAction(Component searchWrapper, JComponent searchTextField, Supplier<String> propertyNameSupplier) {
        super("Search History", null, AllIcons.Actions.SearchWithHistory);
        this.searchWrapper = searchWrapper;
        this.searchTextField = searchTextField;
        this.propertyNameSupplier = propertyNameSupplier;
        registerCustomShortcutSet(KeymapUtil.getActiveKeymapShortcuts("ShowSearchHistory"), searchTextField);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        showCompletionPopup(getHistory(e.getProject()));
    }

    private void showCompletionPopup(List<String> list) {
        JBPopupFactory.getInstance()
                .createPopupChooserBuilder(list)
                .setMovable(false)
                .setResizable(false)
                .setRequestFocus(true)
                .setItemChosenCallback(item -> {
                    UIUtils.setText(searchTextField, item);
                    IdeFocusManager.getGlobalInstance().requestFocus(searchTextField, false);
                })
                .createPopup()
                .showUnderneathOf(searchWrapper);
    }

    public List<String> getHistory(Project project) {
        String history = PropertiesComponent.getInstance(project).getValue(propertyNameSupplier.get());
        return StrUtil.isNotBlank(history) ? StrUtil.split(history, '\n') : List.of();
    }

}