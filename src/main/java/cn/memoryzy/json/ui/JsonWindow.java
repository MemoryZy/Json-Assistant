package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.ui.basic.MultiRowLanguageTextField;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.json5.Json5Language;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.ui.LanguageTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class JsonWindow {
    private JPanel rootPanel;
    private LanguageTextField jsonTextField;

    private final Project project;

    public JsonWindow(Project project) {
        this.project = project;
    }

    private void createUIComponents() {
        jsonTextField = new MultiRowLanguageTextField(Json5Language.INSTANCE, project, "", false);
        jsonTextField.setFont(new Font("Consolas", Font.PLAIN, 15));
        jsonTextField.getDocument().addDocumentListener(new DocumentListenerImpl());

        String value = PropertiesComponent.getInstance(project).getValue(PluginConstant.JSON_VIEWER_LAST_RECORD);
        if (StrUtil.isNotBlank(value)) {
            jsonTextField.setText(value);
        }
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public LanguageTextField getJsonTextField() {
        return jsonTextField;
    }

    public String getJsonContent() {
        return jsonTextField.getText();
    }

    private class DocumentListenerImpl implements DocumentListener {

        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            String text = jsonTextField.getText();
            if (StrUtil.isNotBlank(text)) {
                PropertiesComponent.getInstance(project).setValue(PluginConstant.JSON_VIEWER_LAST_RECORD, text);
            }
        }
    }
}
