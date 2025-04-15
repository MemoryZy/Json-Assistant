package cn.memoryzy.json.ui.dialog;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.DataTypeConstant;
import cn.memoryzy.json.constant.HtmlConstant;
import cn.memoryzy.json.constant.LanguageHolder;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.model.BlacklistEntry;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.ClipboardDataBlacklistPersistentState;
import cn.memoryzy.json.ui.editor.ViewerModeLanguageTextEditor;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.UIManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.LinkedList;
import java.util.Objects;

/**
 * @author Memory
 * @since 2025/3/2
 */
public class PreviewClipboardDataDialog extends DialogWrapper {

    private final Project project;
    private final EditorEx editor;
    private final String parseType;
    private final String jsonString;
    private final String originalText;
    private final boolean isJson5;
    private ViewerModeLanguageTextEditor showTextField;


    public PreviewClipboardDataDialog(@Nullable Project project, EditorEx editor, String parseType, String jsonString, String originalText) {
        super(project, true);
        this.project = project;
        this.editor = editor;
        this.parseType = parseType;
        this.jsonString = jsonString;
        this.originalText = originalText;
        this.isJson5 = DataTypeConstant.JSON5.equals(parseType);

        // setModal(false);
        setTitle(JsonAssistantBundle.messageOnSystem("dialog.preview.clipboard.title"));
        setOKButtonText(JsonAssistantBundle.messageOnSystem("dialog.history.ok"));
        setCancelButtonText(JsonAssistantBundle.messageOnSystem("dialog.history.cancel"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        boolean isJson = DataTypeConstant.JSON.equals(parseType) || isJson5;
        String key = isJson ? "dialog.preview.clipboard.tip" : "dialog.preview.clipboard.tipWithTransform";

        JBLabel tipLabel = new JBLabel(HtmlConstant.wrapHtml(JsonAssistantBundle.messageOnSystem(key, parseType)));
        tipLabel.setBorder(JBUI.Borders.emptyLeft(5));
        TitledSeparator titledSeparator = new TitledSeparator();

        BorderLayoutPanel topPanel = new BorderLayoutPanel().addToTop(tipLabel).addToCenter(titledSeparator);

        if (isJson) {
            titledSeparator.setBorder(JBUI.Borders.empty(5, 0, 8, 5));

        } else {
            titledSeparator.setBorder(JBUI.Borders.empty(3, 0, 0, 5));

            JBLabel tipImportLabel = new JBLabel(HtmlConstant.wrapHtml(JsonAssistantBundle.messageOnSystem("dialog.preview.clipboard.tipImport")));
            // tipImportLabel.setIcon(AllIcons.Actions.IntentionBulb);
            tipImportLabel.setBorder(JBUI.Borders.empty(4, 5, 6, 0));
            topPanel.addToBottom(tipImportLabel);
        }

        BorderLayoutPanel rootPanel = new BorderLayoutPanel().addToTop(topPanel).addToCenter(showTextField);
        rootPanel.setPreferredSize(new JBDimension(400, 420));
        return rootPanel;
    }

    @Override
    protected void doOKAction() {
        if (getOKAction().isEnabled()) {
            // 执行逻辑
            executeOkAction();
            close(OK_EXIT_CODE);
        }
    }

    @Override
    public void doCancelAction() {
        if (getCancelAction().isEnabled()) {
            // 添加被拒绝的JSON
            JsonWrapper wrapper = isJson5 ? Json5Util.parse(jsonString) : JsonUtil.parse(jsonString);
            String text = isJson5 ? showTextField.getText() : originalText;
            addToBlacklist(parseType, text, wrapper);
            close(CANCEL_EXIT_CODE);
        }
    }

    private void executeOkAction() {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            String text = isJson5 ? showTextField.getText() : jsonString;
            PlatformUtil.setDocumentText(editor.getDocument(), text);
        });
    }

    @Override
    protected void init() {
        // 如果是JSON5，即保留注释
        String value = isJson5 ? originalText : jsonString;
        this.showTextField = new ViewerModeLanguageTextEditor(LanguageHolder.JSON5, project, value, true);
        this.showTextField.setFont(UIManager.consolasFont(13));
        this.showTextField.addNotify();

        if (isJson5) {
            // 进行格式化
            PlatformUtil.reformatText(showTextField.getEditor());
        }

        super.init();
    }

    @Override
    public void show() {
        ApplicationManager.getApplication().invokeLater(super::show);
    }

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return UrlType.RECOGNIZE.getId();
    }


    public static void addToBlacklist(String parseType, String originalText, JsonWrapper wrapper) {
        LinkedList<BlacklistEntry> blacklist = ClipboardDataBlacklistPersistentState.getInstance().blacklist;
        Integer id = blacklist.stream().map(BlacklistEntry::getId).max(Integer::compareTo).orElse(-1);
        blacklist.addFirst(new BlacklistEntry(id + 1, originalText, parseType, wrapper));
    }

    public static boolean existsInBlacklist(JsonWrapper wrapper) {
        LinkedList<BlacklistEntry> blacklist = ClipboardDataBlacklistPersistentState.getInstance().blacklist;
        return blacklist.stream().anyMatch(el -> Objects.equals(wrapper, el.getJsonWrapper()));
    }
}
