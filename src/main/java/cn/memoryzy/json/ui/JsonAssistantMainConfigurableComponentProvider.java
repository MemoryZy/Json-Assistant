package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.AttributeSerializationPersistentState;
import cn.memoryzy.json.service.persistent.EditorOptionsPersistentState;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/10/23
 */
public class JsonAssistantMainConfigurableComponentProvider {

    private JPanel rootPanel;
    private JLabel attributeSerializationLabel;
    private JBCheckBox includeRandomValuesCb;
    private JBLabel includeRandomValuesLabel;
    private JBLabel includeRandomValuesDesc;
    private JLabel toolWindowLabel;
    private JBCheckBox loadLastRecordCb;
    private JBLabel loadLastRecordLabel;
    private JBLabel loadLastRecordDesc;
    private JBCheckBox followEditorThemeCb;
    private JBLabel followEditorThemeLabel;
    private JBCheckBox displayLineNumbersCb;
    private JBLabel displayLineNumbersLabel;
    private JBCheckBox foldingOutlineCb;
    private JBLabel foldingOutlineLabel;
    private JBCheckBox fastJsonCb;
    private JBLabel fastJsonLabel;
    private JBLabel fastJsonDesc;
    private JBCheckBox jacksonCb;
    private JBLabel jacksonLabel;
    private JBLabel jacksonDesc;

    private final EditorOptionsPersistentState editorOptionsPersistentState = EditorOptionsPersistentState.getInstance();
    private final AttributeSerializationPersistentState attributeSerializationPersistentState = AttributeSerializationPersistentState.getInstance();

    public JsonAssistantMainConfigurableComponentProvider() {
        initAttributeSerializationComponent();
        initToolWindowComponent();
    }

    private void initAttributeSerializationComponent() {
        // ---------------- 属性序列化
        attributeSerializationLabel.setText(JsonAssistantBundle.messageOnSystem("plugin.main.configurable.component.attribute.serialization.text"));
        // ---------- 包含随机值
        includeRandomValuesLabel.setText(JsonAssistantBundle.messageOnSystem("plugin.main.configurable.component.random.value.label.text"));
        includeRandomValuesCb.setSelected(attributeSerializationPersistentState.includeRandomValues);

        includeRandomValuesDesc.setCopyable(true);
        includeRandomValuesDesc.setForeground(JBColor.GRAY);
        includeRandomValuesDesc.setFont(JBUI.Fonts.smallFont());
        includeRandomValuesDesc.setText(JsonAssistantBundle.messageOnSystem("plugin.main.configurable.component.random.value.desc"));

        // ---------- 识别 FastJson 注解
        fastJsonLabel.setText(JsonAssistantBundle.messageOnSystem("plugin.main.configurable.component.fastjson.label.text"));
        fastJsonCb.setSelected(attributeSerializationPersistentState.recognitionFastJsonAnnotation);

        fastJsonDesc.setCopyable(true);
        fastJsonDesc.setForeground(JBColor.GRAY);
        fastJsonDesc.setFont(JBUI.Fonts.smallFont());
        fastJsonDesc.setText(JsonAssistantBundle.messageOnSystem("plugin.main.configurable.component.fastjson.desc"));

        jacksonLabel.setText(JsonAssistantBundle.messageOnSystem("plugin.main.configurable.component.jackson.label.text"));
        jacksonCb.setSelected(attributeSerializationPersistentState.recognitionJacksonAnnotation);

        jacksonDesc.setCopyable(true);
        jacksonDesc.setForeground(JBColor.GRAY);
        jacksonDesc.setFont(JBUI.Fonts.smallFont());
        jacksonDesc.setText(JsonAssistantBundle.messageOnSystem("plugin.main.configurable.component.jackson.desc"));

        addAttributeSerializationComponentClickListener();
    }

    private void initToolWindowComponent() {
        // ---------------- 工具窗口
        toolWindowLabel.setText(JsonAssistantBundle.messageOnSystem("plugin.main.configurable.component.tool.window.text"));
        // ---------- 导入最新 JSON 记录
        loadLastRecordCb.setSelected(editorOptionsPersistentState.loadLastRecord);
        loadLastRecordLabel.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.load.last.record.cb.text"));

        loadLastRecordDesc.setCopyable(true);
        loadLastRecordDesc.setForeground(JBColor.GRAY);
        loadLastRecordDesc.setFont(JBUI.Fonts.smallFont());
        loadLastRecordDesc.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.load.last.record.cb.tip.text"));

        // ---------- 跟随主编辑器配色主题
        followEditorThemeCb.setSelected(editorOptionsPersistentState.followEditorTheme);
        followEditorThemeLabel.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.follow.editor.theme.cb.text"));

        // ---------- 展示编辑器行号
        displayLineNumbersCb.setSelected(editorOptionsPersistentState.displayLineNumbers);
        displayLineNumbersLabel.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.display.lines.cb.text"));

        // ---------- 显示折叠轮廓
        foldingOutlineCb.setSelected(editorOptionsPersistentState.foldingOutline);
        foldingOutlineLabel.setText(JsonAssistantBundle.messageOnSystem("editor.options.configurable.component.folding.outline.cb.text"));

        addToolWindowComponentClickListener();
    }

    private void addAttributeSerializationComponentClickListener() {
        includeRandomValuesLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                includeRandomValuesCb.setSelected(!includeRandomValuesCb.isSelected());
            }
        });

        fastJsonLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fastJsonCb.setSelected(!fastJsonCb.isSelected());
            }
        });

        jacksonLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                jacksonCb.setSelected(!jacksonCb.isSelected());
            }
        });
    }

    private void addToolWindowComponentClickListener() {
        loadLastRecordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                loadLastRecordCb.setSelected(!loadLastRecordCb.isSelected());
            }
        });

        followEditorThemeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                followEditorThemeCb.setSelected(!followEditorThemeCb.isSelected());
            }
        });

        displayLineNumbersLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                displayLineNumbersCb.setSelected(!displayLineNumbersCb.isSelected());
            }
        });

        foldingOutlineLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                foldingOutlineCb.setSelected(!foldingOutlineCb.isSelected());
            }
        });
    }

    public JComponent createRootPanel() {
        return rootPanel;
    }

    public void reset() {
        // 恢复为初始状态
        includeRandomValuesCb.setSelected(attributeSerializationPersistentState.includeRandomValues);
        fastJsonCb.setSelected(attributeSerializationPersistentState.recognitionFastJsonAnnotation);
        jacksonCb.setSelected(attributeSerializationPersistentState.recognitionJacksonAnnotation);

        loadLastRecordCb.setSelected(editorOptionsPersistentState.loadLastRecord);
        followEditorThemeCb.setSelected(editorOptionsPersistentState.followEditorTheme);
        displayLineNumbersCb.setSelected(editorOptionsPersistentState.displayLineNumbers);
        foldingOutlineCb.setSelected(editorOptionsPersistentState.foldingOutline);
    }

    public boolean isModified() {
        boolean oldIncludeRandomValues = attributeSerializationPersistentState.includeRandomValues;
        boolean oldRecognitionFastJsonAnnotation = attributeSerializationPersistentState.recognitionFastJsonAnnotation;
        boolean oldRecognitionJacksonAnnotation = attributeSerializationPersistentState.recognitionJacksonAnnotation;

        boolean oldLoadLastRecord = editorOptionsPersistentState.loadLastRecord;
        boolean oldFollowEditorTheme = editorOptionsPersistentState.followEditorTheme;
        boolean oldDisplayLineNumbers = editorOptionsPersistentState.displayLineNumbers;
        boolean oldFoldingOutline = editorOptionsPersistentState.foldingOutline;

        boolean includeRandomValues = includeRandomValuesCb.isSelected();
        boolean recognitionFastJsonAnnotation = fastJsonCb.isSelected();
        boolean recognitionJacksonAnnotation = jacksonCb.isSelected();

        boolean loadLastRecord = loadLastRecordCb.isSelected();
        boolean followEditorTheme = followEditorThemeCb.isSelected();
        boolean displayLineNumbers = displayLineNumbersCb.isSelected();
        boolean foldingOutline = foldingOutlineCb.isSelected();

        return !Objects.equals(oldIncludeRandomValues, includeRandomValues)
                || !Objects.equals(oldRecognitionFastJsonAnnotation, recognitionFastJsonAnnotation)
                || !Objects.equals(oldRecognitionJacksonAnnotation, recognitionJacksonAnnotation)

                || !Objects.equals(oldLoadLastRecord, loadLastRecord)
                || !Objects.equals(oldFollowEditorTheme, followEditorTheme)
                || !Objects.equals(oldDisplayLineNumbers, displayLineNumbers)
                || !Objects.equals(oldFoldingOutline, foldingOutline);
    }

    public void apply() {
        boolean includeRandomValues = includeRandomValuesCb.isSelected();
        boolean recognitionFastJsonAnnotation = fastJsonCb.isSelected();
        boolean recognitionJacksonAnnotation = jacksonCb.isSelected();

        boolean loadLastRecord = loadLastRecordCb.isSelected();
        boolean followEditorTheme = followEditorThemeCb.isSelected();
        boolean displayLineNumbers = displayLineNumbersCb.isSelected();
        boolean foldingOutline = foldingOutlineCb.isSelected();

        attributeSerializationPersistentState.includeRandomValues = includeRandomValues;
        attributeSerializationPersistentState.recognitionFastJsonAnnotation = recognitionFastJsonAnnotation;
        attributeSerializationPersistentState.recognitionJacksonAnnotation = recognitionJacksonAnnotation;

        editorOptionsPersistentState.loadLastRecord = loadLastRecord;
        editorOptionsPersistentState.followEditorTheme = followEditorTheme;
        editorOptionsPersistentState.displayLineNumbers = displayLineNumbers;
        editorOptionsPersistentState.foldingOutline = foldingOutline;
    }

}
