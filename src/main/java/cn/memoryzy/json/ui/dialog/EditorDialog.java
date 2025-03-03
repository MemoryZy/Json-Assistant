package cn.memoryzy.json.ui.dialog;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.LanguageHolder;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.model.JsonEntry;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.ClipboardDataBlacklistPersistentState;
import cn.memoryzy.json.ui.decorator.TextEditorErrorPopupDecorator;
import cn.memoryzy.json.ui.editor.CustomizedLanguageTextEditor;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.LinkedList;

/**
 * @author Memory
 * @since 2025/3/3
 */
public class EditorDialog extends DialogWrapper {

    private final CustomizedLanguageTextEditor languageTextEditor;
    private final TextEditorErrorPopupDecorator jsonErrorDecorator;

    public EditorDialog(@Nullable Project project) {
        super(project, true);
        languageTextEditor = new CustomizedLanguageTextEditor(LanguageHolder.JSON5, project, "", true);
        languageTextEditor.setShowPlaceholderWhenFocused(true);
        languageTextEditor.setPlaceholder(JsonAssistantBundle.messageOnSystem("dialog.editor.placeholder"));

        this.jsonErrorDecorator = new TextEditorErrorPopupDecorator(getRootPane(), languageTextEditor);

        setTitle(JsonAssistantBundle.messageOnSystem("dialog.editor.title"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        languageTextEditor.addNotify();
        BorderLayoutPanel panel = new BorderLayoutPanel().addToCenter(languageTextEditor);
        panel.setPreferredSize(JBUI.size(400, 450));
        return panel;
    }

    @Override
    protected void doOKAction() {
        if (getOKAction().isEnabled()) {
            // 执行逻辑
            if (executeOkAction()) {
                close(OK_EXIT_CODE);
            }
        }
    }

    private boolean executeOkAction() {
        String text = languageTextEditor.getText();
        if (StrUtil.isBlank(text)) {
            jsonErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.invalid.json"));
            return false;
        }

        boolean isJson = JsonUtil.isJson(text);
        boolean isJson5 = Json5Util.isJson5(text);

        if (!isJson && !isJson5) {
            jsonErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.invalid.json"));
            return false;
        }

        JsonWrapper wrapper = isJson ? JsonUtil.parse(text) : Json5Util.parse(text);
        LinkedList<JsonEntry> blacklist = ClipboardDataBlacklistPersistentState.getInstance().blacklist;
        Integer id = blacklist.stream().map(JsonEntry::getId).max(Integer::compareTo).orElse(-1);
        blacklist.addFirst(new JsonEntry(id + 1, wrapper));

        return true;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return languageTextEditor;
    }

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return UrlType.DEFAULT.getId();
    }

    @Override
    public void show() {
        ApplicationManager.getApplication().invokeLater(super::show);
    }

}
