package cn.memoryzy.json.ui.dialog;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.DataTypeConstant;
import cn.memoryzy.json.constant.HtmlConstant;
import cn.memoryzy.json.constant.LanguageHolder;
import cn.memoryzy.json.ui.editor.ViewerModeLanguageTextEditor;
import cn.memoryzy.json.util.UIManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2025/3/2
 */
public class PreviewClipboardDataDialog extends DialogWrapper {

    private final Project project;
    private final String parseType;

    public PreviewClipboardDataDialog(@Nullable Project project, String parseType) {
        super(project, true);
        this.project = project;
        this.parseType = parseType;

        setModal(false);
        setTitle(JsonAssistantBundle.messageOnSystem("dialog.preview.clipboard.title"));
        setOKButtonText(JsonAssistantBundle.messageOnSystem("dialog.history.ok"));
        setCancelButtonText(JsonAssistantBundle.messageOnSystem("dialog.history.cancel"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        String key = DataTypeConstant.JSON.equals(parseType) || DataTypeConstant.JSON5.equals(parseType)
                ? "dialog.preview.clipboard.tip"
                : "dialog.preview.clipboard.tipWithTransform";

        JBLabel tipLabel = new JBLabel(HtmlConstant.wrapHtml(JsonAssistantBundle.messageOnSystem(key, parseType)));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tipLabel, BorderLayout.CENTER);

        panel.setBorder(JBUI.Borders.empty(4, 5, 7, 0));

        String json = "{\n" +
                "  \"ui\": \"89\"\n" +
                "}";

        ViewerModeLanguageTextEditor showTextField = new ViewerModeLanguageTextEditor(LanguageHolder.JSON5, project, json, true);
        showTextField.setFont(UIManager.consolasFont(13));
        // 通知创建Editor
        showTextField.addNotify();

        BorderLayoutPanel rootPanel = new BorderLayoutPanel().addToTop(panel).addToCenter(showTextField);
        rootPanel.setPreferredSize(new JBDimension(390, 410));
        return rootPanel;
    }
}
