package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.EditorOptionsPersistentState;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/9/12
 */
public class EditorOptionsConfigurableComponentProvider {

    private JPanel rootPanel;
    private JLabel generalLabel;
    private JBCheckBox loadLastRecordCb;
    private JBLabel loadLastRecordTextLabel;
    private JLabel editorLabel;
    private JBCheckBox followEditorThemeCb;
    private JBLabel followEditorThemeTextLabel;
    private JBCheckBox displayLineNumbersCb;
    private JBLabel displayLineNumbersTextLabel;
    private JBCheckBox foldingOutlineCb;
    private JBLabel foldingOutlineTextLabel;
    private JBLabel loadLastRecordTipLabel;

    private final EditorOptionsPersistentState persistentState = EditorOptionsPersistentState.getInstance();

    public EditorOptionsConfigurableComponentProvider() {
        generalLabel.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.general.text"));
        loadLastRecordCb.setSelected(persistentState.loadLastRecord);
        loadLastRecordTextLabel.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.load.last.record.cb.text"));

        loadLastRecordTipLabel.setForeground(JBColor.GRAY);
        loadLastRecordTipLabel.setFont(JBUI.Fonts.smallFont());
        loadLastRecordTipLabel.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.load.last.record.cb.tip.text"));

        // --------------------- 编辑器区域
        editorLabel.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.editor.text"));
        followEditorThemeCb.setSelected(persistentState.followEditorTheme);
        followEditorThemeTextLabel.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.follow.editor.theme.cb.text"));

        displayLineNumbersCb.setSelected(persistentState.displayLineNumbers);
        displayLineNumbersTextLabel.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.display.lines.cb.text"));

        foldingOutlineCb.setSelected(persistentState.foldingOutline);
        foldingOutlineTextLabel.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.folding.outline.cb.text"));
    }

    public JComponent createRootPanel() {
        return rootPanel;
    }

    public void reset() {
        // 恢复为初始状态
        loadLastRecordCb.setSelected(persistentState.loadLastRecord);
        followEditorThemeCb.setSelected(persistentState.followEditorTheme);
        displayLineNumbersCb.setSelected(persistentState.displayLineNumbers);
        foldingOutlineCb.setSelected(persistentState.foldingOutline);
    }

    public boolean isModified() {
        boolean oldLoadLastRecord = persistentState.loadLastRecord;
        boolean oldFollowEditorTheme = persistentState.followEditorTheme;
        boolean oldDisplayLineNumbers = persistentState.displayLineNumbers;
        boolean oldFoldingOutline = persistentState.foldingOutline;

        boolean loadLastRecord = loadLastRecordCb.isSelected();
        boolean followEditorTheme = followEditorThemeCb.isSelected();
        boolean displayLineNumbers = displayLineNumbersCb.isSelected();
        boolean foldingOutline = foldingOutlineCb.isSelected();

        return !Objects.equals(oldLoadLastRecord, loadLastRecord)
                || !Objects.equals(oldFollowEditorTheme, followEditorTheme)
                || !Objects.equals(oldDisplayLineNumbers, displayLineNumbers)
                || !Objects.equals(oldFoldingOutline, foldingOutline);
    }

    public void apply() {
        boolean loadLastRecord = loadLastRecordCb.isSelected();
        boolean followEditorTheme = followEditorThemeCb.isSelected();
        boolean displayLineNumbers = displayLineNumbersCb.isSelected();
        boolean foldingOutline = foldingOutlineCb.isSelected();

        // 导入历史记录
        persistentState.loadLastRecord = loadLastRecord;
        persistentState.followEditorTheme = followEditorTheme;
        persistentState.displayLineNumbers = displayLineNumbers;
        persistentState.foldingOutline = foldingOutline;
    }
}
