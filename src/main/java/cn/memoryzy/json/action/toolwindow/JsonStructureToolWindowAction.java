package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.LanguageTextField;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

public class JsonStructureToolWindowAction extends DumbAwareAction implements UpdateInBackground {

    private final LanguageTextField languageTextField;

    public JsonStructureToolWindowAction(LanguageTextField languageTextField, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.languageTextField = languageTextField;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.json.structure.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.structure.on.tw.title.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.STRUCTURE);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt T"), simpleToolWindowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        String text = StrUtil.trim(languageTextField.getText());
        JsonAssistantUtil.showJsonStructureDialog(text);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(JsonAssistantUtil.isJsonOrExtract(languageTextField.getText()));
    }

}