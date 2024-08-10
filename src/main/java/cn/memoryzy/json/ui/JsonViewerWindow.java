package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.ui.basic.CustomizedLanguageTextEditor;
import cn.memoryzy.json.ui.basic.JsonViewerPanel;
import cn.memoryzy.json.utils.JsonUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.json.json5.Json5Language;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class JsonViewerWindow {

    private LanguageTextField jsonTextField;
    private final Project project;
    private JsonViewerHistoryState historyState;

    public JsonViewerWindow(Project project) {
        this.project = project;
    }

    public JPanel getRootPanel() {
        this.jsonTextField = new CustomizedLanguageTextEditor(Json5Language.INSTANCE, project, "", false);
        this.jsonTextField.setFont(new Font("Consolas", Font.PLAIN, 15));
        this.jsonTextField.getDocument().addDocumentListener(new DocumentListenerImpl());
        this.jsonTextField.addFocusListener(new FocusListenerImpl());
        this.historyState = JsonViewerHistoryState.getInstance(project);
        JsonViewerPanel rootPanel = new JsonViewerPanel(new BorderLayout(), this.jsonTextField);

        String jsonStr = "";
        String clipboard = PlatformUtil.getClipboard();
        if (StrUtil.isNotBlank(clipboard)) {
            jsonStr = (JsonUtil.isJsonStr(clipboard)) ? clipboard : JsonUtil.extractJsonStr(clipboard);
        }

        if (StrUtil.isNotBlank(jsonStr)) {
            jsonTextField.setText(jsonStr);
        } else {
            List<String> historyList = historyState.historyList;
            int historySize = historyList.size();

            if (historySize > 0) {
                jsonTextField.setText(historyList.get(historySize - 1));
            }
        }

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(JBUI.Borders.empty(1, 2));
        centerPanel.add(jsonTextField, BorderLayout.CENTER);
        rootPanel.add(centerPanel, BorderLayout.CENTER);

        return rootPanel;
    }

    public String getJsonContent() {
        return jsonTextField.getText();
    }

    public LanguageTextField getJsonTextField() {
        return jsonTextField;
    }


    private class DocumentListenerImpl implements DocumentListener {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            List<String> historyList = historyState.historyList;
            String text = jsonTextField.getText();
            if (!historyList.contains(text) && JsonUtil.isJsonStr(text)) {
                historyList.add(text);
            }
        }
    }

    private class FocusListenerImpl implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            String text = jsonTextField.getText();
            if (StrUtil.isBlank(text)) {
                String clipboard = PlatformUtil.getClipboard();
                if (StrUtil.isNotBlank(clipboard)) {
                    String jsonStr = (JsonUtil.isJsonStr(clipboard)) ? clipboard : JsonUtil.extractJsonStr(clipboard);
                    jsonTextField.setText(jsonStr);
                }
            }
        }

        @Override
        public void focusLost(FocusEvent e) {

        }
    }

}
