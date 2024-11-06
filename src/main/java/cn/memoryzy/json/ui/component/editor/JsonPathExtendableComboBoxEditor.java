package cn.memoryzy.json.ui.component.editor;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.component.editor.extension.SearchExtension;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.JBFont;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 * @author Memory
 * @since 2024/8/19
 */
public class JsonPathExtendableComboBoxEditor extends BasicComboBoxEditor {

    private final Runnable action;
    private final JBFont font;

    public JsonPathExtendableComboBoxEditor(Runnable action, JBFont font) {
        super();
        this.action = action;
        this.font = font;
    }

    @Override
    public JTextField createEditorComponent() {
        ExtendableTextField editor = new ExtendableTextField(20);
        editor.addExtension(new SearchExtension(action));
        editor.getEmptyText().setText(JsonAssistantBundle.messageOnSystem("dialog.json.path.text.field.placeholder"));
        editor.setFont(font);
        editor.setBorder(null);
        return editor;
    }

    public ExtendableTextField getEditorTextField() {
        return (ExtendableTextField) editor;
    }

    public String getEditorText() {
        return getEditorTextField().getText();
    }

    public void setEditorText(String text) {
        getEditorTextField().setText(text);
    }
}
