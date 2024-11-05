package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.BackgroundColorPolicy;
import cn.memoryzy.json.service.persistent.AttributeSerializationPersistentState;
import cn.memoryzy.json.service.persistent.EditorOptionsPersistentState;
import cn.memoryzy.json.ui.dialog.SupportDialog;
import cn.memoryzy.json.util.UIManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import icons.JsonAssistantIcons;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/10/31
 */
public class JsonAssistantMainConfigurableComponentProvider {
    // region 组件
    private JPanel rootPanel;
    private TitledSeparator attributeSerializationLabel;
    private JBCheckBox includeRandomValuesCb;
    private JBLabel includeRandomValuesDesc;
    private JBCheckBox fastJsonCb;
    private JBLabel fastJsonDesc;
    private JBCheckBox jacksonCb;
    private JBLabel jacksonDesc;
    private TitledSeparator windowBehaviorLabel;
    private JBCheckBox importHistoryCb;
    private JBLabel importHistoryDesc;
    private JBCheckBox displayLineNumbersCb;
    private JBCheckBox foldingOutlineCb;
    private ActionLink donateLink;
    private JBCheckBox recognizeOtherFormatsCb;
    private JBLabel recognizeOtherFormatsDesc;
    private JBCheckBox xmlFormatsCb;
    private JBCheckBox yamlFormatsCb;
    private JBCheckBox tomlFormatsCb;
    private JBCheckBox urlParamFormatsCb;
    private JPanel formatCbPanel;
    private JBLabel backgroundColorTitle;
    private ComboBox<BackgroundColorPolicy> backgroundColorBox;
    private TitledSeparator windowAppearanceLabel;
    // endregion

    private final EditorOptionsPersistentState editorOptionsState = EditorOptionsPersistentState.getInstance();
    private final AttributeSerializationPersistentState attributeSerializationState = AttributeSerializationPersistentState.getInstance();

    public JPanel createRootPanel() {
        applyAttributeSerializationChunk();
        applyToolWindowBehaviorChunk();
        applyToolWindowAppearanceChunk();
        applyDonateLinkChunk();

        addSwitchListener();
        // 初始化
        reset();

        return rootPanel;
    }


    private void applyAttributeSerializationChunk() {
        attributeSerializationLabel.setText(JsonAssistantBundle.messageOnSystem("setting.component.attribute.serialization.text"));

        includeRandomValuesCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.random.value.text"));
        UIManager.setCommentLabel(includeRandomValuesDesc, includeRandomValuesCb, JsonAssistantBundle.messageOnSystem("setting.component.random.value.desc"));

        fastJsonCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.fastjson.text"));
        UIManager.setCommentLabel(fastJsonDesc, fastJsonCb, JsonAssistantBundle.messageOnSystem("setting.component.fastjson.desc"));

        jacksonCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.jackson.text"));
        UIManager.setCommentLabel(jacksonDesc, jacksonCb, JsonAssistantBundle.messageOnSystem("setting.component.jackson.desc"));
    }

    private void applyToolWindowBehaviorChunk() {
        windowBehaviorLabel.setText(JsonAssistantBundle.messageOnSystem("setting.component.window.behavior.text"));

        importHistoryCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.import.history.text"));
        UIManager.setCommentLabel(importHistoryDesc, importHistoryCb, JsonAssistantBundle.messageOnSystem("setting.component.import.history.desc"));

        recognizeOtherFormatsCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.recognize.other.formats.text"));
        UIManager.setHelpLabel(recognizeOtherFormatsDesc, JsonAssistantBundle.messageOnSystem("setting.component.recognize.other.formats.desc"));

        xmlFormatsCb.setText("XML");
        yamlFormatsCb.setText("YAML");
        tomlFormatsCb.setText("TOML");
        urlParamFormatsCb.setText("URL Param");

        int left = UIUtil.getCheckBoxTextHorizontalOffset(recognizeOtherFormatsCb);
        formatCbPanel.setBorder(new JBEmptyBorder(JBUI.insets(1, left, 4, 0)));
    }

    private void applyToolWindowAppearanceChunk() {
        windowAppearanceLabel.setText(JsonAssistantBundle.messageOnSystem("setting.component.window.appearance.text"));

        backgroundColorTitle.setText(JsonAssistantBundle.messageOnSystem("setting.component.background.color.text"));
        for (BackgroundColorPolicy value : BackgroundColorPolicy.values()) {
            backgroundColorBox.addItem(value);
        }

        displayLineNumbersCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.display.lines.text"));
        foldingOutlineCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.folding.outline.text"));
    }

    private void addSwitchListener() {
        recognizeOtherFormatsCb.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                UIManager.controlEnableCheckBox(xmlFormatsCb, true);
                UIManager.controlEnableCheckBox(yamlFormatsCb, true);
                UIManager.controlEnableCheckBox(tomlFormatsCb, true);
                UIManager.controlEnableCheckBox(urlParamFormatsCb, true);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                UIManager.controlEnableCheckBox(xmlFormatsCb, false);
                UIManager.controlEnableCheckBox(yamlFormatsCb, false);
                UIManager.controlEnableCheckBox(tomlFormatsCb, false);
                UIManager.controlEnableCheckBox(urlParamFormatsCb, false);
            }
        });

        // backgroundColorBox.addActionListener(new AbstractAction() {
        //     @Override
        //     public void actionPerformed(ActionEvent e) {
        //         BackgroundColorMatchingEnum item = backgroundColorBox.getItem();
        //         if (BackgroundColorMatchingEnum.CUSTOM.equals(item)) {
        //             Color selectedColor = ColorPicker.showDialog(backgroundColorBox, IdeBundle.message("dialog.title.choose.color"),
        //                     editorOptionsState.customColor, true, null, true);
        //             if (null != selectedColor) {
        //                 color = selectedColor;
        //             }
        //         }
        //     }
        // });
    }

    private void applyDonateLinkChunk() {
        donateLink.setIcon(JsonAssistantIcons.DONATE);
        donateLink.setText(JsonAssistantBundle.messageOnSystem("action.welcome.donate.text"));
        donateLink.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SupportDialog().show();
            }
        });
    }


    public void reset() {
        // 属性序列化
        includeRandomValuesCb.setSelected(attributeSerializationState.includeRandomValues);
        fastJsonCb.setSelected(attributeSerializationState.recognitionFastJsonAnnotation);
        jacksonCb.setSelected(attributeSerializationState.recognitionJacksonAnnotation);

        // 外观
        importHistoryCb.setSelected(editorOptionsState.importHistory);
        backgroundColorBox.setItem(editorOptionsState.backgroundColorPolicy);
        displayLineNumbersCb.setSelected(editorOptionsState.displayLineNumbers);
        foldingOutlineCb.setSelected(editorOptionsState.foldingOutline);

        // 解析
        boolean recognizeOtherFormats = editorOptionsState.recognizeOtherFormats;
        recognizeOtherFormatsCb.setSelected(recognizeOtherFormats);
        xmlFormatsCb.setSelected(editorOptionsState.recognizeXmlFormat);
        yamlFormatsCb.setSelected(editorOptionsState.recognizeYamlFormat);
        tomlFormatsCb.setSelected(editorOptionsState.recognizeTomlFormat);
        urlParamFormatsCb.setSelected(editorOptionsState.recognizeUrlParamFormat);

        if (recognizeOtherFormats) {
            UIManager.controlEnableCheckBox(xmlFormatsCb, true);
            UIManager.controlEnableCheckBox(yamlFormatsCb, true);
            UIManager.controlEnableCheckBox(tomlFormatsCb, true);
            UIManager.controlEnableCheckBox(urlParamFormatsCb, true);
        } else {
            UIManager.controlEnableCheckBox(xmlFormatsCb, false);
            UIManager.controlEnableCheckBox(yamlFormatsCb, false);
            UIManager.controlEnableCheckBox(tomlFormatsCb, false);
            UIManager.controlEnableCheckBox(urlParamFormatsCb, false);
        }
    }


    public boolean isModified() {
        // 属性序列化
        boolean oldIncludeRandomValues = attributeSerializationState.includeRandomValues;
        boolean oldRecognitionFastJsonAnnotation = attributeSerializationState.recognitionFastJsonAnnotation;
        boolean oldRecognitionJacksonAnnotation = attributeSerializationState.recognitionJacksonAnnotation;

        // 外观
        boolean oldImportHistory = editorOptionsState.importHistory;
        BackgroundColorPolicy oldBackgroundColorPolicy = editorOptionsState.backgroundColorPolicy;
        boolean oldDisplayLineNumbers = editorOptionsState.displayLineNumbers;
        boolean oldFoldingOutline = editorOptionsState.foldingOutline;

        // 解析
        boolean oldRecognizeOtherFormats = editorOptionsState.recognizeOtherFormats;
        boolean oldRecognizeXmlFormat = editorOptionsState.recognizeXmlFormat;
        boolean oldRecognizeYamlFormat = editorOptionsState.recognizeYamlFormat;
        boolean oldRecognizeTomlFormat = editorOptionsState.recognizeTomlFormat;
        boolean oldRecognizeUrlParamFormat = editorOptionsState.recognizeUrlParamFormat;

        // ----------------------------------------------------------------------

        // 属性序列化
        boolean includeRandomValues = includeRandomValuesCb.isSelected();
        boolean recognitionFastJsonAnnotation = fastJsonCb.isSelected();
        boolean recognitionJacksonAnnotation = jacksonCb.isSelected();

        // 外观
        boolean importHistory = importHistoryCb.isSelected();
        BackgroundColorPolicy backgroundColorPolicy = backgroundColorBox.getItem();
        boolean displayLineNumbers = displayLineNumbersCb.isSelected();
        boolean foldingOutline = foldingOutlineCb.isSelected();

        // 解析
        boolean recognizeOtherFormats = recognizeOtherFormatsCb.isSelected();
        boolean recognizeXmlFormat = xmlFormatsCb.isSelected();
        boolean recognizeYamlFormat = yamlFormatsCb.isSelected();
        boolean recognizeTomlFormat = tomlFormatsCb.isSelected();
        boolean recognizeUrlParamFormat = urlParamFormatsCb.isSelected();

        // 比较是否更改
        return !Objects.equals(oldIncludeRandomValues, includeRandomValues)
                || !Objects.equals(oldRecognitionFastJsonAnnotation, recognitionFastJsonAnnotation)
                || !Objects.equals(oldRecognitionJacksonAnnotation, recognitionJacksonAnnotation)

                || !Objects.equals(oldImportHistory, importHistory)
                || !Objects.equals(oldBackgroundColorPolicy, backgroundColorPolicy)
                || !Objects.equals(oldDisplayLineNumbers, displayLineNumbers)
                || !Objects.equals(oldFoldingOutline, foldingOutline)

                || !Objects.equals(oldRecognizeOtherFormats, recognizeOtherFormats)
                || !Objects.equals(oldRecognizeXmlFormat, recognizeXmlFormat)
                || !Objects.equals(oldRecognizeYamlFormat, recognizeYamlFormat)
                || !Objects.equals(oldRecognizeTomlFormat, recognizeTomlFormat)
                || !Objects.equals(oldRecognizeUrlParamFormat, recognizeUrlParamFormat);
    }

    public void apply() {
        // 属性序列化
        attributeSerializationState.includeRandomValues = includeRandomValuesCb.isSelected();
        attributeSerializationState.recognitionFastJsonAnnotation = fastJsonCb.isSelected();
        attributeSerializationState.recognitionJacksonAnnotation = jacksonCb.isSelected();

        // 外观
        editorOptionsState.importHistory = importHistoryCb.isSelected();
        editorOptionsState.displayLineNumbers = displayLineNumbersCb.isSelected();
        editorOptionsState.foldingOutline = foldingOutlineCb.isSelected();
        editorOptionsState.backgroundColorPolicy = backgroundColorBox.getItem();
        // if (null != color && BackgroundColorMatchingEnum.CUSTOM.equals(colorBoxItem)) {
        //     editorOptionsState.customColor = color;
        // }

        // 解析
        editorOptionsState.recognizeOtherFormats = recognizeOtherFormatsCb.isSelected();
        editorOptionsState.recognizeXmlFormat = xmlFormatsCb.isSelected();
        editorOptionsState.recognizeYamlFormat = yamlFormatsCb.isSelected();
        editorOptionsState.recognizeTomlFormat = tomlFormatsCb.isSelected();
        editorOptionsState.recognizeUrlParamFormat = urlParamFormatsCb.isSelected();
    }

}
