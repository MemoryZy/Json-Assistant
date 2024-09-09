package cn.memoryzy.json.ui.component.editor;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.EditorComboBoxEditor;
import com.intellij.ui.EditorTextField;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/19
 */
public class JsonPathFileTypeComboBoxEditor extends EditorComboBoxEditor {

    private final Project project;

    public JsonPathFileTypeComboBoxEditor(Project project, FileType fileType, Font font) {
        super(project, fileType);
        this.project = project;
        initEditorTextField(font);

        String documentName = Objects.equals(PlainTextFileType.INSTANCE, fileType) ? "a.dummy" : "a.jsonpath";
        final PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(documentName, fileType, "", 0, true);
        final Document document = PsiDocumentManager.getInstance(project).getDocument(file);
        assert document != null;

        super.setItem(document);
    }

    @Override
    public EditorTextField getEditorComponent() {
        return super.getEditorComponent();
    }

    @NotNull
    @Override
    public Object getItem() {
        return ((Document) super.getItem()).getText();
    }

    @Override
    public void setItem(Object anObject) {
        if (anObject == null) anObject = "";
        if (anObject.equals(getItem())) return;
        final String s = (String) anObject;
        WriteCommandAction.writeCommandAction(project).run(() -> getDocument().setText(s));

        final Editor editor = getEditor();
        if (editor != null) editor.getCaretModel().moveToOffset(s.length());
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
        editorTextField.setShowPlaceholderWhenFocused(true);
    }
}
