package cn.memoryzy.json.ui.basic.jsonpath;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorComboBoxEditor;
import com.intellij.ui.EditorTextField;

import java.awt.*;

/**
 * @author Memory
 * @since 2024/8/19
 */
public class JsonPathFileTypeComboBoxEditor extends EditorComboBoxEditor {

    public JsonPathFileTypeComboBoxEditor(Project project, FileType fileType, Font font) {
        super(project, fileType);
        initEditorTextField(font);
    }

    @Override
    public EditorTextField getEditorComponent() {
        return super.getEditorComponent();
    }

    public void setEditorText(String text) {
        getEditorComponent().setText(text);
    }

    public String getEditorText() {
        return getEditorComponent().getText();
    }

    private void initEditorTextField(Font font) {
        EditorTextField editorTextField = getEditorComponent();
        editorTextField.setFont(font);
        editorTextField.setPlaceholder(JsonAssistantBundle.messageOnSystem("dialog.json.path.text.field.placeholder"));
    }
}
