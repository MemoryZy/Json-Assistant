package cn.memoryzy.json.ui.dialog;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.GeneralSettings;
import cn.memoryzy.json.service.persistent.state.TreeStructureState;
import cn.memoryzy.json.ui.decorator.EditorErrorPopupManager;
import cn.memoryzy.json.ui.editor.ExpandableEditorTextField;
import com.intellij.json.json5.Json5Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.util.function.Supplier;

/**
 * @author Memory
 * @since 2026/4/17
 */
public class ModifyNodeValueDialog extends DialogWrapper {

    private final TreeStructureState structureState;;
    private final ExpandableEditorTextField expandableTextField;
    private final JBCheckBox sourceFileCheckBox;
    private final EditorErrorPopupManager editorErrorManager;
    private Supplier<Boolean> okAction;

    public ModifyNodeValueDialog(@Nullable Project project, Object defaultValue) {
        super(project, true);
        this.structureState = GeneralSettings.getInstance().getState().getTreeStructureState();
        this.expandableTextField = new ExpandableEditorTextField(project, Json5Language.INSTANCE);
        this.sourceFileCheckBox = new JBCheckBox();
        this.editorErrorManager = new EditorErrorPopupManager(getRootPane(), expandableTextField);

        init(defaultValue);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        BorderLayoutPanel centerPanel = new BorderLayoutPanel().addToCenter(expandableTextField);
        centerPanel.setPreferredSize(new Dimension(330, 30));
        return centerPanel;
    }

    @Override
    protected @Nullable JPanel createSouthAdditionalPanel() {
        return new BorderLayoutPanel().addToCenter(sourceFileCheckBox);
    }

    @Override
    protected void doOKAction() {
        if (getOKAction().isEnabled()) {
            // 执行逻辑
            if (okAction.get()) {
                close(OK_EXIT_CODE);
            }
        }
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return expandableTextField;
    }

    public void setError(String error) {
        editorErrorManager.setError(error);
    }

    private void init(Object defaultValue) {
        // 如果源数据是 String 类型，就加上 双引号
        this.expandableTextField.setText((defaultValue instanceof String) ? "\"" + defaultValue + "\"" : String.valueOf(defaultValue));
        this.expandableTextField.selectAll();
        this.expandableTextField.setShowPlaceholderWhenFocused(true);
        this.expandableTextField.setPlaceholder(JsonAssistantBundle.messageOnSystem("popup.modifyNodeValue.placeholder"));

        this.sourceFileCheckBox.setText(JsonAssistantBundle.messageOnSystem("popup.modifyNodeValue.applyToSourceFile.text"));
        this.sourceFileCheckBox.setSelected(structureState.isShouldApplyToSource());
        this.sourceFileCheckBox.addActionListener(e -> structureState.setShouldApplyToSource(sourceFileCheckBox.isSelected()));

        init();
        setTitle(JsonAssistantBundle.messageOnSystem("dialog.modify.node.value.title"));
        setOKButtonText(JsonAssistantBundle.messageOnSystem("dialog.ok.text"));
        setCancelButtonText(JsonAssistantBundle.messageOnSystem("dialog.cancel.text"));
    }

    public void addEditorKeyListener(KeyListener listener) {
        this.expandableTextField.addKeyListener(listener);
    }

    public ExpandableEditorTextField getExpandableTextField() {
        return expandableTextField;
    }

    public void setOkAction(Supplier<Boolean> okAction) {
        this.okAction = okAction;
    }
}
