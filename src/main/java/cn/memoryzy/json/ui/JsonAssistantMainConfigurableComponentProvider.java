package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.BackgroundColorScheme;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.AttributeSerializationState;
import cn.memoryzy.json.service.persistent.state.EditorAppearanceState;
import cn.memoryzy.json.service.persistent.state.EditorBehaviorState;
import cn.memoryzy.json.ui.color.CircleIcon;
import cn.memoryzy.json.ui.dialog.SupportDialog;
import cn.memoryzy.json.util.UIManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.ColorPicker;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import icons.JsonAssistantIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
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
    private ComboBox<BackgroundColorScheme> backgroundColorBox;
    private TitledSeparator windowAppearanceLabel;
    private JBLabel backgroundColorDesc;
    // endregion

    private Color selectedCustomColor;

    /**
     * 标志变量，用于标识是否处于初始加载状态
     */
    private boolean isLoading = false;
    private final JsonAssistantPersistentState persistentState = JsonAssistantPersistentState.getInstance();

    public JPanel createRootPanel() {
        applyAttributeSerializationChunk();
        applyToolWindowBehaviorChunk();
        applyToolWindowAppearanceChunk();
        applyDonateLinkChunk();

        addSwitchListener();
        setRenderer();
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

        xmlFormatsCb.setText("Xml");
        yamlFormatsCb.setText("Yaml");
        tomlFormatsCb.setText("Toml");
        urlParamFormatsCb.setText("URL Param");

        int left = UIUtil.getCheckBoxTextHorizontalOffset(recognizeOtherFormatsCb);
        formatCbPanel.setBorder(new JBEmptyBorder(JBUI.insets(1, left, 4, 0)));
    }

    private void applyToolWindowAppearanceChunk() {
        windowAppearanceLabel.setText(JsonAssistantBundle.messageOnSystem("setting.component.window.appearance.text"));

        backgroundColorTitle.setText(JsonAssistantBundle.messageOnSystem("setting.component.background.color.text"));
        for (BackgroundColorScheme value : BackgroundColorScheme.values()) {
            backgroundColorBox.addItem(value);
        }

        // TODO 这里要改描述
        UIManager.setHelpLabel(backgroundColorDesc, JsonAssistantBundle.messageOnSystem("setting.component.background.color.desc"));
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

        backgroundColorBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isLoading = false;
            }
        });

        backgroundColorBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BackgroundColorScheme item = backgroundColorBox.getItem();
                if (BackgroundColorScheme.Custom.equals(item) && !isLoading) {
                    backgroundColorBox.hidePopup();
                    boolean darkTheme = UIUtil.isUnderDarcula();
                    EditorAppearanceState editorAppearanceState = persistentState.editorAppearanceState;
                    Color preselectedColor = darkTheme ? editorAppearanceState.customDarkcolor : editorAppearanceState.customLightColor;
                    String title = darkTheme ? JsonAssistantBundle.messageOnSystem("dialog.choose.dark.color.title") : JsonAssistantBundle.messageOnSystem("dialog.choose.light.color.title");

                    Color selectedColor = ColorPicker.showDialog(
                            backgroundColorBox, title,
                            preselectedColor, true, null, true);

                    if (null != selectedColor) {
                        selectedCustomColor = selectedColor;
                    }
                }
            }
        });
    }

    private void applyDonateLinkChunk() {
        donateLink.setIcon(JsonAssistantIcons.DONATE);
        donateLink.setText(JsonAssistantBundle.messageOnSystem("action.donate.welcome.text"));
        donateLink.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SupportDialog().show();
            }
        });
    }


    private void setRenderer() {
        backgroundColorBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                // 调用父类方法
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                BackgroundColorScheme colorScheme = (BackgroundColorScheme) value;

                setIcon(BackgroundColorScheme.Custom.equals(colorScheme)
                        // 创建一个空白图标
                        ? new ImageIcon(new BufferedImage(14, 14, BufferedImage.TYPE_INT_ARGB))
                        // 创建圆形图标
                        : new CircleIcon(14, colorScheme.getColor()));

                return this;
            }
        });
    }


    // ----------------------------------------------------------------------------------- //

    public void reset() {
        // 属性序列化
        AttributeSerializationState attributeSerializationState = persistentState.attributeSerializationState;
        includeRandomValuesCb.setSelected(attributeSerializationState.includeRandomValues);
        fastJsonCb.setSelected(attributeSerializationState.recognitionFastJsonAnnotation);
        jacksonCb.setSelected(attributeSerializationState.recognitionJacksonAnnotation);

        // 行为
        EditorBehaviorState editorBehaviorState = persistentState.editorBehaviorState;
        importHistoryCb.setSelected(editorBehaviorState.importHistory);
        // 控制全部CheckBox
        boolean recognizeOtherFormats = editorBehaviorState.recognizeOtherFormats;
        recognizeOtherFormatsCb.setSelected(recognizeOtherFormats);
        xmlFormatsCb.setSelected(editorBehaviorState.recognizeXmlFormat);
        yamlFormatsCb.setSelected(editorBehaviorState.recognizeYamlFormat);
        tomlFormatsCb.setSelected(editorBehaviorState.recognizeTomlFormat);
        urlParamFormatsCb.setSelected(editorBehaviorState.recognizeUrlParamFormat);

        // 外观
        EditorAppearanceState editorAppearanceState = persistentState.editorAppearanceState;
        applyBackgroundColorItem(editorAppearanceState);
        displayLineNumbersCb.setSelected(editorAppearanceState.displayLineNumbers);
        foldingOutlineCb.setSelected(editorAppearanceState.foldingOutline);

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

    private void applyBackgroundColorItem(EditorAppearanceState editorAppearanceState) {
        // 在初始化组件、Reset时，不需要弹出颜色选择窗
        isLoading = true;
        backgroundColorBox.setItem(editorAppearanceState.backgroundColorScheme);
        selectedCustomColor = UIUtil.isUnderDarcula() ? editorAppearanceState.customDarkcolor : editorAppearanceState.customLightColor;
        isLoading = false;
    }

    public boolean isModified() {
        // 属性序列化
        AttributeSerializationState attributeSerializationState = persistentState.attributeSerializationState;
        boolean oldIncludeRandomValues = attributeSerializationState.includeRandomValues;
        boolean oldRecognitionFastJsonAnnotation = attributeSerializationState.recognitionFastJsonAnnotation;
        boolean oldRecognitionJacksonAnnotation = attributeSerializationState.recognitionJacksonAnnotation;

        // 行为
        EditorBehaviorState editorBehaviorState = persistentState.editorBehaviorState;
        boolean oldImportHistory = editorBehaviorState.importHistory;
        boolean oldRecognizeOtherFormats = editorBehaviorState.recognizeOtherFormats;
        boolean oldRecognizeXmlFormat = editorBehaviorState.recognizeXmlFormat;
        boolean oldRecognizeYamlFormat = editorBehaviorState.recognizeYamlFormat;
        boolean oldRecognizeTomlFormat = editorBehaviorState.recognizeTomlFormat;
        boolean oldRecognizeUrlParamFormat = editorBehaviorState.recognizeUrlParamFormat;

        // 外观
        EditorAppearanceState editorAppearanceState = persistentState.editorAppearanceState;
        boolean oldDisplayLineNumbers = editorAppearanceState.displayLineNumbers;
        boolean oldFoldingOutline = editorAppearanceState.foldingOutline;
        BackgroundColorScheme oldBackgroundColorScheme = editorAppearanceState.backgroundColorScheme;
        Color oldColor = UIUtil.isUnderDarcula() ? editorAppearanceState.customDarkcolor : editorAppearanceState.customLightColor;

        // ----------------------------------------------------------------------

        // 属性序列化
        boolean includeRandomValues = includeRandomValuesCb.isSelected();
        boolean recognitionFastJsonAnnotation = fastJsonCb.isSelected();
        boolean recognitionJacksonAnnotation = jacksonCb.isSelected();

        // 外观
        boolean importHistory = importHistoryCb.isSelected();
        BackgroundColorScheme backgroundColorScheme = backgroundColorBox.getItem();
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
                || !Objects.equals(oldBackgroundColorScheme, backgroundColorScheme)
                || (BackgroundColorScheme.Custom.equals(backgroundColorScheme) && !Objects.equals(oldColor, selectedCustomColor))

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
        AttributeSerializationState attributeSerializationState = persistentState.attributeSerializationState;
        attributeSerializationState.includeRandomValues = includeRandomValuesCb.isSelected();
        attributeSerializationState.recognitionFastJsonAnnotation = fastJsonCb.isSelected();
        attributeSerializationState.recognitionJacksonAnnotation = jacksonCb.isSelected();

        // 行为
        EditorBehaviorState editorBehaviorState = persistentState.editorBehaviorState;
        editorBehaviorState.importHistory = importHistoryCb.isSelected();
        editorBehaviorState.recognizeOtherFormats = recognizeOtherFormatsCb.isSelected();
        editorBehaviorState.recognizeXmlFormat = xmlFormatsCb.isSelected();
        editorBehaviorState.recognizeYamlFormat = yamlFormatsCb.isSelected();
        editorBehaviorState.recognizeTomlFormat = tomlFormatsCb.isSelected();
        editorBehaviorState.recognizeUrlParamFormat = urlParamFormatsCb.isSelected();

        // 外观
        EditorAppearanceState editorAppearanceState = persistentState.editorAppearanceState;
        editorAppearanceState.displayLineNumbers = displayLineNumbersCb.isSelected();
        editorAppearanceState.foldingOutline = foldingOutlineCb.isSelected();
        BackgroundColorScheme selectedScheme = backgroundColorBox.getItem();
        editorAppearanceState.backgroundColorScheme = selectedScheme;

        // 如果选择的是Custom，那么将选择的颜色赋值给customColor，这个color可以暂时缓存起来
        if (BackgroundColorScheme.Custom.equals(selectedScheme)) {
            if (UIUtil.isUnderDarcula()) {
                editorAppearanceState.customDarkcolor = selectedCustomColor;
            } else {
                editorAppearanceState.customLightColor = selectedCustomColor;
            }
        }
    }

}
