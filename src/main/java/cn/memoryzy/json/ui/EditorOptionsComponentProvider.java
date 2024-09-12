package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.EditorOptionsPersistentState;
import com.intellij.ui.components.JBCheckBox;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/9/12
 */
public class EditorOptionsComponentProvider {

    private JPanel rootPanel;
    private JLabel titleLabel;
    private JBCheckBox followEditorThemeCb;
    private final EditorOptionsPersistentState persistentState;

    public EditorOptionsComponentProvider(EditorOptionsPersistentState persistentState) {
        this.persistentState = persistentState;
        titleLabel.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.title"));
        followEditorThemeCb.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.follow.editor.theme.cb.text"));
        followEditorThemeCb.setSelected(persistentState.followEditorTheme);
    }

    public JComponent createRootPanel() {

        return rootPanel;
    }

    public void reset() {

    }

    public boolean isModified() {


        return false;
    }

    public void apply() {

    }
}
