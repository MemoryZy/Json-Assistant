package cn.memoryzy.json.action;

import com.intellij.find.FindBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.IPopupChooserBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * @author Memory
 * @since 2024/8/21
 */
public class ShowHistoryAction extends DumbAwareAction {

    private final String historyPropertyName;

    public ShowHistoryAction(String historyPropertyName, JComponent searchTextField) {
        super(FindBundle.message("find.search.history"), null, AllIcons.Actions.SearchWithHistory);
        this.historyPropertyName = historyPropertyName;
        registerCustomShortcutSet(KeymapUtil.getActiveKeymapShortcuts("ShowSearchHistory"), searchTextField);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {


    }

    private void showCompletionPopup(List<String> list){
        IPopupChooserBuilder<String> builder = JBPopupFactory.getInstance().createPopupChooserBuilder(list);
        // builder
        //         .setMovable(false)
        //         .setResizable(false)
        //         .setRequestFocus(true)
        //         .setItemSelectedCallback()
        //         .setItemChoosenCallback(Runnable {
        //     val selectedValue = list.selectedValue
        //     if (selectedValue != null) {
        //         textField.text = selectedValue
        //         IdeFocusManager.getGlobalInstance().requestFocus(textField, false)
        //     }
        // })
        // .createPopup()

    }

}