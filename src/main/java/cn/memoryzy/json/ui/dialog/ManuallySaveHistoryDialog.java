package cn.memoryzy.json.ui.dialog;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.HistoryLimitedList;
import cn.memoryzy.json.ui.decorator.TextEditorErrorPopupDecorator;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * @author Memory
 * @since 2025/4/7
 */
public class ManuallySaveHistoryDialog extends DialogWrapper {

    private final HistoryLimitedList historyList;
    private final LanguageTextField languageTextField;
    private final TextEditorErrorPopupDecorator editorErrorDecorator;

    private String newName;

    public ManuallySaveHistoryDialog(@NotNull Project project, HistoryLimitedList historyList, String oldName) {
        super(project, true);
        this.historyList = historyList;
        this.languageTextField = new LanguageTextField(PlainTextLanguage.INSTANCE, project, (null == oldName ? "" : oldName));
        this.editorErrorDecorator = new TextEditorErrorPopupDecorator(getRootPane(), languageTextField);

        setTitle(JsonAssistantBundle.messageOnSystem("dialog.assign.history.name.title"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        languageTextField.setShowPlaceholderWhenFocused(true);
        languageTextField.setPlaceholder("Name");
        languageTextField.getDocument().addDocumentListener(new ValidateSameName());

        BorderLayoutPanel borderLayoutPanel = new BorderLayoutPanel().addToTop(languageTextField);
        borderLayoutPanel.setPreferredSize(new JBDimension(280, 35));

        return borderLayoutPanel;
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
        String nameText = StrUtil.trim(languageTextField.getText());
        if (StrUtil.isBlank(nameText)) {
            editorErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.ManuallySaveHistory.blank"));
            return false;
        }

        if (nameText.length() > 50) {
            editorErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.ManuallySaveHistory.tooLong"));
            return false;
        }

        this.newName = nameText;
        return true;
    }

    public String getNewName() {
        return newName;
    }

    private class ValidateSameName implements DocumentListener {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            String name = languageTextField.getText();
            if (historyList.stream().anyMatch(el -> Objects.equals(name, el.getName()))) {
                editorErrorDecorator.setWarning(JsonAssistantBundle.messageOnSystem("warning.ManuallySaveHistory.sameName"));
            }
        }
    }

}
