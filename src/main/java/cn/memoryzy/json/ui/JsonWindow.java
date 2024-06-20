package cn.memoryzy.json.ui;

import cn.memoryzy.json.ui.basic.MultiRowLanguageTextField;
import com.intellij.json.json5.Json5Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.LanguageTextField;

import javax.swing.*;
import java.awt.*;

/**
 * @author wcp
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
}
