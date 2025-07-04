package cn.memoryzy.json.ui.panel;

import cn.memoryzy.json.ui.component.EditorButton;
import cn.memoryzy.json.ui.editor.BorderlessEditorTextField;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * @author Memory
 * @since 2025/7/1
 */
public class EditWrapper extends TransparentContainer {

    private final EditTextField editTextField;

    public EditWrapper(Project project, String editActionName) {
        super(new BorderLayout());
        this.editTextField = new EditTextField(project, PlainTextFileType.INSTANCE);
        this.initComponents(editActionName);
    }

    private void initComponents(String editActionName) {
        AnAction action = new AnAction(editActionName, null, JsonAssistantIcons.ToolWindow.EDIT) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
            }
        };

        EditorButton editorButton = new EditorButton(action, false);

        JPanel editorWrapper = new NonOpaquePanel(new BorderLayout());
        editorWrapper.setBorder(JBUI.Borders.empty(3, 6));
        editorWrapper.add(editorButton, BorderLayout.NORTH);

        add(editorWrapper, BorderLayout.WEST);
        add(editTextField, BorderLayout.CENTER);
        setBorder(JBUI.Borders.customLine(JBColor.border(), 1, 1, 1, 1));
    }

    public void setText(String text) {
        editTextField.setText(text);
    }

    public String getText() {
        return editTextField.getText();
    }

    public void setPlaceholder(String text) {
        editTextField.setPlaceholder(text);
    }

    public void setShowPlaceholderWhenFocused(boolean flag) {
        editTextField.setShowPlaceholderWhenFocused(flag);
    }

    public JComponent getPreferredFocusedComponent() {
        return editTextField;
    }

    public Editor getEditor() {
        return editTextField.getEditor();
    }

    public void moveToOffset(int offset) {
        Optional.ofNullable(editTextField.getEditor())
                .map(Editor::getCaretModel)
                .ifPresent(caretModel -> caretModel.moveToOffset(offset));
    }


    private static class EditTextField extends BorderlessEditorTextField {
        public EditTextField(Project project, FileType fileType) {
            super(project, fileType);
        }

        @Override
        protected @NotNull EditorEx createEditor() {
            EditorEx editorEx = super.createEditor();
            JComponent component = editorEx.getComponent();
            component.setBorder(JBUI.Borders.empty(5, 0, 3, 6));
            return editorEx;
        }
    }
}
