package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.service.JsonViewRecordState;
import cn.memoryzy.json.ui.basic.MyLanguageTextEditor;
import cn.memoryzy.json.utils.JsonUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.json.json5.Json5Language;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.ui.LanguageTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class JsonViewWindow {
    private JPanel rootPanel;
    private LanguageTextField jsonTextField;
    private final Project project;
    private JsonViewRecordState state;

    public JsonViewWindow(Project project) {
        this.project = project;
    }

    private void createUIComponents() {
        jsonTextField = new MyLanguageTextEditor(Json5Language.INSTANCE, project, "", false);
        jsonTextField.setFont(new Font("Consolas", Font.PLAIN, 15));
        jsonTextField.getDocument().addDocumentListener(new DocumentListenerImpl());
        jsonTextField.addFocusListener(new FocusListenerImpl());
        this.state = JsonViewRecordState.getInstance(project);

        String jsonStr = "";
        String clipboard = PlatformUtil.getClipboard();
        if (StrUtil.isNotBlank(clipboard)) {
            jsonStr = (JsonUtil.isJsonStr(clipboard)) ? clipboard : JsonUtil.extractJsonStr(clipboard);
        }

        if (StrUtil.isNotBlank(jsonStr)) {
            jsonTextField.setText(jsonStr);
        } else {
            String record = state.record;
            if (StrUtil.isNotBlank(record)) {
                jsonTextField.setText(record);
            }
        }
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public String getJsonContent() {
        return jsonTextField.getText();
    }

    private class DocumentListenerImpl implements DocumentListener {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            String record = state.record;
            String text = jsonTextField.getText();

            if (!Objects.equals(record, text)) {
                state.record = text;
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
