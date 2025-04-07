package cn.memoryzy.json.ui.dialog;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.HistoryLimitedList;
import cn.memoryzy.json.model.JsonEntry;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.ui.decorator.TextEditorErrorPopupDecorator;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2025/4/7
 */
public class ManuallySaveHistoryDialog extends DialogWrapper {

    private final Project project;
    private final String documentText;
    private final LanguageTextField languageTextField;
    private final TextEditorErrorPopupDecorator editorErrorDecorator;
    private final JsonHistoryPersistentState historyState;

    public ManuallySaveHistoryDialog(@NotNull Project project, String documentText) {
        super(project, true);
        this.project = project;
        this.documentText = documentText;
        this.historyState = JsonHistoryPersistentState.getInstance(project);

        this.languageTextField = new LanguageTextField(PlainTextLanguage.INSTANCE, project, "");
        this.editorErrorDecorator = new TextEditorErrorPopupDecorator(getRootPane(), languageTextField);

        setTitle(JsonAssistantBundle.messageOnSystem("dialog.assign.history.name.title"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        languageTextField.setShowPlaceholderWhenFocused(true);
        languageTextField.setPlaceholder("Name");

        BorderLayoutPanel borderLayoutPanel = new BorderLayoutPanel().addToTop(languageTextField);
        borderLayoutPanel.setPreferredSize(new JBDimension(280, 35));

        return borderLayoutPanel;
    }

    @Override
    public void show() {
        ApplicationManager.getApplication().invokeLater(super::show);
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return languageTextField;
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
        HistoryLimitedList historyList = historyState.getHistory();

        String text = StrUtil.trim(documentText);
        JsonWrapper jsonWrapper = null;
        if (JsonUtil.isJson(text)) {
            jsonWrapper = JsonUtil.parse(text);

        } else if (Json5Util.isJson5(text)) {
            jsonWrapper = Json5Util.parse(text);
        }

        JsonEntry jsonEntry = historyList.filterItem(jsonWrapper);
        String oldName = (null == jsonEntry) ? "" : jsonEntry.getName();
        languageTextField.setText(oldName);

        String nameText = StrUtil.trim(languageTextField.getText());
        if (StrUtil.isBlank(nameText)) {
            editorErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.ManuallySaveHistory.blank"));
            return false;
        }

        if (nameText.length() > 50) {
            editorErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.ManuallySaveHistory.tooLong"));
            return false;
        }


        return false;
    }

}
