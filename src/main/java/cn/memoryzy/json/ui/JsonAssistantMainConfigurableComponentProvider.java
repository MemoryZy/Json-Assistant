package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.ColorScheme;
import cn.memoryzy.json.enums.DataFormatType;
import cn.memoryzy.json.enums.TreeViewMode;
import cn.memoryzy.json.event.*;
import cn.memoryzy.json.service.persistent.GeneralSettings;
import cn.memoryzy.json.service.persistent.SerializationSettings;
import cn.memoryzy.json.service.persistent.ToolWindowSettings;
import cn.memoryzy.json.service.persistent.state.*;
import cn.memoryzy.json.ui.dialog.SupportDialog;
import cn.memoryzy.json.ui.icon.CircleIcon;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Set;

/**
 * @author Memory
 * @since 2024/10/31
 */
public class JsonAssistantMainConfigurableComponentProvider {

    // region 组件
    private JPanel rootPanel;
    private TitledSeparator serializationTitle;
    private JBCheckBox serializeRandomValuesCheckBox;
    private JBLabel serializeRandomValuesDesc;
    private JBCheckBox detectFastJsonAnnotationsCheckBox;
    private JBLabel detectFastJsonAnnotationsDesc;
    private JBCheckBox detectJacksonAnnotationsCheckBox;
    private JBLabel detectJacksonAnnotationsDesc;

    private TitledSeparator editorBehaviorTitle;
    private JBCheckBox showLineNumbersCheckBox;
    private JBCheckBox showFoldingOutlineCheckBox;
    private JBCheckBox autoRecognizeFormatsCheckBox;
    private JBLabel autoRecognizeFormatsDesc;
    private JBCheckBox enableXmlFormatsCheckBox;
    private JBCheckBox enableYamlFormatsCheckBox;
    private JBCheckBox enableTomlFormatsCheckBox;
    private JBCheckBox enableTypeScriptFormatsCheckBox;
    private JBCheckBox enableUrlParamFormatsCheckBox;
    private JPanel formatsCheckBoxPanel;

    private TitledSeparator editorVisualTitle;
    private JBLabel backgroundLabel;
    private ComboBox<ColorScheme> backgroundComboBox;
    private JPanel backgroundPanel;

    private TitledSeparator historyTitle;
    private JBCheckBox enableHistoryCheckBox;

    private TitledSeparator generalTitle;
    private JBLabel treeViewModeLabel;
    private JBLabel treeViewModeDesc;
    private ComboBox<TreeViewMode> treeViewModeComboBox;
    private JBLabel autoRecordHistoryLabel;
    private JBRadioButton autoRecordHistoryRadioBtn;
    private JBRadioButton manualRecordHistoryRadioBtn;
    private JBLabel autoRecordHistoryDesc;

    private ActionLink donateLink;
    private JBCheckBox applyToSourceCheckBox;
    private JBLabel applyToSourceDesc;
    // endregion


    private final boolean isIdea = PlatformUtil.isIdea();


    public JPanel createComponent() {
        configureGeneralComponents();
        configureSerializationComponents();
        configureToolWindowBehaviorComponents();
        configureToolWindowAppearanceComponents();
        configureHistoryComponents();
        configureDonateLinkComponents();
        // 初始化
        reset();
        return rootPanel;
    }

    /**
     * 常规
     */
    private void configureGeneralComponents() {
        generalTitle.setText(JsonAssistantBundle.messageOnSystem("setting.component.general.text"));
        treeViewModeLabel.setText(JsonAssistantBundle.messageOnSystem("setting.component.tree.display.mode.text"));
        UIUtils.setHelpLabel(treeViewModeDesc, JsonAssistantBundle.messageOnSystem("setting.component.tree.display.mode.desc"));

        for (TreeViewMode value : TreeViewMode.values()) {
            treeViewModeComboBox.addItem(value);
        }

        treeViewModeComboBox.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@NotNull JList<? extends TreeViewMode> list, TreeViewMode value, int index, boolean selected, boolean hasFocus) {
                setText(value != null ? JsonAssistantBundle.messageOnSystem(value.getKey()) : "");
            }
        });
    }

    /**
     * 属性序列化
     */
    private void configureSerializationComponents() {
        serializationTitle.setText(JsonAssistantBundle.messageOnSystem("setting.component.attribute.serialization.text"));

        serializeRandomValuesCheckBox.setText(JsonAssistantBundle.messageOnSystem("setting.component.random.value.text"));
        UIUtils.setCommentLabel(serializeRandomValuesDesc, serializeRandomValuesCheckBox, JsonAssistantBundle.messageOnSystem("setting.component.random.value.desc"));

        detectFastJsonAnnotationsCheckBox.setText(JsonAssistantBundle.messageOnSystem("setting.component.fastjson.text"));
        UIUtils.setCommentLabel(detectFastJsonAnnotationsDesc, detectFastJsonAnnotationsCheckBox, JsonAssistantBundle.messageOnSystem("setting.component.fastjson.desc"));

        detectJacksonAnnotationsCheckBox.setText(JsonAssistantBundle.messageOnSystem("setting.component.jackson.text"));
        UIUtils.setCommentLabel(detectJacksonAnnotationsDesc, detectJacksonAnnotationsCheckBox, JsonAssistantBundle.messageOnSystem("setting.component.jackson.desc"));
    }

    /**
     * 窗口行为
     */
    private void configureToolWindowBehaviorComponents() {
        editorBehaviorTitle.setText(JsonAssistantBundle.messageOnSystem("setting.component.window.behavior.text"));

        autoRecognizeFormatsCheckBox.setText(JsonAssistantBundle.messageOnSystem("setting.component.recognize.other.formats.text"));
        UIUtils.setHelpLabel(autoRecognizeFormatsDesc, JsonAssistantBundle.messageOnSystem("setting.component.recognize.other.formats.desc"));

        enableXmlFormatsCheckBox.setText("XML");
        enableYamlFormatsCheckBox.setText("YAML");
        enableTomlFormatsCheckBox.setText("TOML");
        enableTypeScriptFormatsCheckBox.setText("TypeScript");
        enableUrlParamFormatsCheckBox.setText("URL Param");

        int left = UIUtil.getCheckBoxTextHorizontalOffset(autoRecognizeFormatsCheckBox);
        formatsCheckBoxPanel.setBorder(new JBEmptyBorder(JBUI.insets(1, left, 4, 0)));

        autoRecognizeFormatsCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                UIUtils.controlEnableCheckBox(enableXmlFormatsCheckBox, true);
                UIUtils.controlEnableCheckBox(enableYamlFormatsCheckBox, true);
                UIUtils.controlEnableCheckBox(enableTomlFormatsCheckBox, true);
                UIUtils.controlEnableCheckBox(enableTypeScriptFormatsCheckBox, true);
                UIUtils.controlEnableCheckBox(enableUrlParamFormatsCheckBox, true);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                UIUtils.controlEnableCheckBox(enableXmlFormatsCheckBox, false);
                UIUtils.controlEnableCheckBox(enableYamlFormatsCheckBox, false);
                UIUtils.controlEnableCheckBox(enableTomlFormatsCheckBox, false);
                UIUtils.controlEnableCheckBox(enableTypeScriptFormatsCheckBox, false);
                UIUtils.controlEnableCheckBox(enableUrlParamFormatsCheckBox, false);
            }
        });

        applyToSourceCheckBox.setText(JsonAssistantBundle.messageOnSystem("setting.component.apply.to.source.text"));
        UIUtils.setCommentLabel(applyToSourceDesc, applyToSourceCheckBox, JsonAssistantBundle.messageOnSystem("setting.component.apply.to.source.desc"));
    }

    /**
     * 窗口外观
     */
    private void configureToolWindowAppearanceComponents() {
        editorVisualTitle.setText(JsonAssistantBundle.messageOnSystem("setting.component.window.appearance.text"));
        showLineNumbersCheckBox.setText(JsonAssistantBundle.messageOnSystem("setting.component.display.lines.text"));
        showFoldingOutlineCheckBox.setText(JsonAssistantBundle.messageOnSystem("setting.component.folding.outline.text"));

        if (isIdea) {
            backgroundLabel.setText(JsonAssistantBundle.messageOnSystem("setting.component.background.color.text"));
            for (ColorScheme value : ColorScheme.values()) {
                backgroundComboBox.addItem(value);
            }

            backgroundComboBox.setRenderer(new SimpleListCellRenderer<>() {
                @Override
                public void customize(@NotNull JList<? extends ColorScheme> list, ColorScheme value, int index, boolean selected, boolean hasFocus) {
                    setText(JsonAssistantBundle.messageOnSystem(value.getKey()));
                    Color color = value.getColor();

                    setIcon(Objects.isNull(color)
                            // 创建一个空白图标
                            ? new ImageIcon(new BufferedImage(14, 14, BufferedImage.TYPE_INT_ARGB))
                            // 创建圆形图标
                            : new CircleIcon(14, color));
                }
            });
        } else {
            backgroundPanel.setVisible(false);
        }
    }


    /**
     * 历史记录
     */
    private void configureHistoryComponents() {
        historyTitle.setText(JsonAssistantBundle.messageOnSystem("setting.component.history.text"));
        enableHistoryCheckBox.setText(JsonAssistantBundle.messageOnSystem("setting.component.history.record.text"));

        autoRecordHistoryLabel.setText(JsonAssistantBundle.messageOnSystem("setting.component.history.auto.store.text"));
        autoRecordHistoryRadioBtn.setText(JsonAssistantBundle.messageOnSystem("setting.component.history.auto.text"));
        manualRecordHistoryRadioBtn.setText(JsonAssistantBundle.messageOnSystem("setting.component.history.manual.text"));
        UIUtils.setHelpLabel(autoRecordHistoryDesc, JsonAssistantBundle.messageOnSystem("setting.component.history.auto.store.desc"));

        ButtonGroup group = new ButtonGroup();
        group.add(autoRecordHistoryRadioBtn);
        group.add(manualRecordHistoryRadioBtn);

        enableHistoryCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                UIUtils.controlEnableRadioButton(autoRecordHistoryRadioBtn, true);
                UIUtils.controlEnableRadioButton(manualRecordHistoryRadioBtn, true);

            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                UIUtils.controlEnableRadioButton(autoRecordHistoryRadioBtn, false);
                UIUtils.controlEnableRadioButton(manualRecordHistoryRadioBtn, false);
            }
        });
    }

    /**
     * 支持/捐赠
     */
    private void configureDonateLinkComponents() {
        donateLink.setIcon(JsonAssistantIcons.DONATE);
        donateLink.setText(JsonAssistantBundle.messageOnSystem("action.donate.welcome.text"));
        donateLink.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SupportDialog().show();
            }
        });
    }


    // ----------------------------------------------------------------------------------- //

    public void reset() {
        // ----------------------------------- 属性序列化
        SerializationState serializationState = SerializationSettings.getInstance().getSerializationState();
        serializeRandomValuesCheckBox.setSelected(serializationState.isSerializeRandomValues());
        detectFastJsonAnnotationsCheckBox.setSelected(serializationState.isDetectFastJsonAnnotations());
        detectJacksonAnnotationsCheckBox.setSelected(serializationState.isDetectJacksonAnnotations());

        // ----------------------------------- 行为
        ToolWindowSettings toolWindowSettings = ToolWindowSettings.getInstance();
        EditorBehaviorState editorBehaviorState = toolWindowSettings.getBehaviorState();

        boolean autoRecognizeFormats = editorBehaviorState.isAutoRecognizeFormats();
        autoRecognizeFormatsCheckBox.setSelected(autoRecognizeFormats);

        Set<DataFormatType> enabledFormats = editorBehaviorState.getEnabledFormats();
        enableXmlFormatsCheckBox.setSelected(enabledFormats.contains(DataFormatType.XML));
        enableYamlFormatsCheckBox.setSelected(enabledFormats.contains(DataFormatType.YAML));
        enableTomlFormatsCheckBox.setSelected(enabledFormats.contains(DataFormatType.TOML));
        enableTypeScriptFormatsCheckBox.setSelected(enabledFormats.contains(DataFormatType.TYPE_SCRIPT));
        enableUrlParamFormatsCheckBox.setSelected(enabledFormats.contains(DataFormatType.URL_PARAM));

        applyToSourceCheckBox.setSelected(editorBehaviorState.isShouldApplyToSource());

        // ----------------------------------- 外观
        EditorVisualState visualState = toolWindowSettings.getVisualState();
        showLineNumbersCheckBox.setSelected(visualState.isShowLineNumbers());
        showFoldingOutlineCheckBox.setSelected(visualState.isShowFoldingOutline());
        backgroundComboBox.setItem(visualState.getColorScheme());

        // ----------------------------------- 历史记录
        HistoryState historyState = toolWindowSettings.getHistoryState();
        boolean enableHistory = historyState.isEnableHistory();

        enableHistoryCheckBox.setSelected(enableHistory);
        if (historyState.isAutoRecordHistory()) {
            autoRecordHistoryRadioBtn.setSelected(true);
        } else {
            manualRecordHistoryRadioBtn.setSelected(true);
        }


        // ----------------------------------- 样式处理

        // ---------------------------- 历史记录
        if (enableHistory) {
            UIUtils.controlEnableRadioButton(autoRecordHistoryRadioBtn, true);
            UIUtils.controlEnableRadioButton(manualRecordHistoryRadioBtn, true);

        } else {
            UIUtils.controlEnableRadioButton(autoRecordHistoryRadioBtn, false);
            UIUtils.controlEnableRadioButton(manualRecordHistoryRadioBtn, false);
        }

        // ---------------------------- 解析格式
        if (autoRecognizeFormats) {
            UIUtils.controlEnableCheckBox(enableXmlFormatsCheckBox, true);
            UIUtils.controlEnableCheckBox(enableYamlFormatsCheckBox, true);
            UIUtils.controlEnableCheckBox(enableTomlFormatsCheckBox, true);
            UIUtils.controlEnableCheckBox(enableTypeScriptFormatsCheckBox, true);
            UIUtils.controlEnableCheckBox(enableUrlParamFormatsCheckBox, true);
        } else {
            UIUtils.controlEnableCheckBox(enableXmlFormatsCheckBox, false);
            UIUtils.controlEnableCheckBox(enableYamlFormatsCheckBox, false);
            UIUtils.controlEnableCheckBox(enableTomlFormatsCheckBox, false);
            UIUtils.controlEnableCheckBox(enableTypeScriptFormatsCheckBox, false);
            UIUtils.controlEnableCheckBox(enableUrlParamFormatsCheckBox, false);
        }

        // 常规
        TreeStructureState treeStructureState = GeneralSettings.getInstance().getState().getTreeStructureState();
        treeViewModeComboBox.setItem(treeStructureState.getTreeViewMode());
    }

    public boolean isModified() {
        // ----------------------------------- 序列化
        SerializationState serializationState = SerializationSettings.getInstance().getSerializationState();
        boolean oldSerializeRandomValues = serializationState.isSerializeRandomValues();
        boolean oldDetectFastJsonAnnotations = serializationState.isDetectFastJsonAnnotations();
        boolean oldDetectJacksonAnnotations = serializationState.isDetectJacksonAnnotations();

        // ----------------------------------- 行为
        ToolWindowSettings toolWindowSettings = ToolWindowSettings.getInstance();
        EditorBehaviorState editorBehaviorState = toolWindowSettings.getBehaviorState();
        boolean oldAutoRecognizeFormats = editorBehaviorState.isAutoRecognizeFormats();

        Set<DataFormatType> enabledFormats = editorBehaviorState.getEnabledFormats();
        boolean oldEnableXmlFormat = enabledFormats.contains(DataFormatType.XML);
        boolean oldEnableYamlFormat = enabledFormats.contains(DataFormatType.YAML);
        boolean oldEnableTomlFormat = enabledFormats.contains(DataFormatType.TOML);
        boolean oldEnableTsFormat = enabledFormats.contains(DataFormatType.TYPE_SCRIPT);
        boolean oldEnableUrlParamFormat = enabledFormats.contains(DataFormatType.URL_PARAM);

        boolean oldShouldApplyToSource = editorBehaviorState.isShouldApplyToSource();

        // ----------------------------------- 外观
        EditorVisualState visualState = toolWindowSettings.getVisualState();
        boolean oldShowLineNumbers = visualState.isShowLineNumbers();
        boolean oldShowFoldingOutline = visualState.isShowFoldingOutline();
        ColorScheme oldColorScheme = visualState.getColorScheme();

        // ----------------------------------- 历史记录
        HistoryState historyState = toolWindowSettings.getHistoryState();
        boolean oldEnableHistory = historyState.isEnableHistory();
        boolean oldAutoRecordHistory = historyState.isAutoRecordHistory();

        // ----------------------------------- 常规
        TreeStructureState treeStructureState = GeneralSettings.getInstance().getState().getTreeStructureState();
        TreeViewMode oldTreeViewMode = treeStructureState.getTreeViewMode();


        // ----------------------------------------------------------------------

        // ----------------------------------- 属性序列化
        boolean newSerializeRandomValues = serializeRandomValuesCheckBox.isSelected();
        boolean newDetectFastJsonAnnotations = detectFastJsonAnnotationsCheckBox.isSelected();
        boolean newDetectJacksonAnnotations = detectJacksonAnnotationsCheckBox.isSelected();

        // ----------------------------------- 外观
        boolean newShowLineNumbers = showLineNumbersCheckBox.isSelected();
        boolean newShowFoldingOutline = showFoldingOutlineCheckBox.isSelected();
        ColorScheme newColorScheme = backgroundComboBox.getItem();

        // ----------------------------------- 行为
        boolean newAutoRecognizeFormats = autoRecognizeFormatsCheckBox.isSelected();
        boolean newEnableXmlFormat = enableXmlFormatsCheckBox.isSelected();
        boolean newEnableYamlFormat = enableYamlFormatsCheckBox.isSelected();
        boolean newEnableTomlFormat = enableTomlFormatsCheckBox.isSelected();
        boolean newEnableTsFormat = enableTypeScriptFormatsCheckBox.isSelected();
        boolean newEnableUrlParamFormat = enableUrlParamFormatsCheckBox.isSelected();
        boolean newShouldApplyToSource = applyToSourceCheckBox.isSelected();

        // ----------------------------------- 历史记录
        boolean newEnableHistory = enableHistoryCheckBox.isSelected();
        boolean newAutoRecordHistory = autoRecordHistoryRadioBtn.isSelected();

        // ----------------------------------- 常规
        TreeViewMode newTreeViewMode = treeViewModeComboBox.getItem();

        // ------------------------------------------- 对比

        return !Objects.equals(oldSerializeRandomValues, newSerializeRandomValues)
                || !Objects.equals(oldDetectFastJsonAnnotations, newDetectFastJsonAnnotations)
                || !Objects.equals(oldDetectJacksonAnnotations, newDetectJacksonAnnotations)

                || (isIdea && !Objects.equals(oldColorScheme, newColorScheme))

                || !Objects.equals(oldShowLineNumbers, newShowLineNumbers)
                || !Objects.equals(oldShowFoldingOutline, newShowFoldingOutline)

                || !Objects.equals(oldAutoRecognizeFormats, newAutoRecognizeFormats)
                || !Objects.equals(oldEnableXmlFormat, newEnableXmlFormat)
                || !Objects.equals(oldEnableYamlFormat, newEnableYamlFormat)
                || !Objects.equals(oldEnableTomlFormat, newEnableTomlFormat)
                || !Objects.equals(oldEnableTsFormat, newEnableTsFormat)
                || !Objects.equals(oldEnableUrlParamFormat, newEnableUrlParamFormat)
                || !Objects.equals(oldShouldApplyToSource, newShouldApplyToSource)

                || !Objects.equals(oldEnableHistory, newEnableHistory)
                || !Objects.equals(oldAutoRecordHistory, newAutoRecordHistory)
                || !Objects.equals(oldTreeViewMode, newTreeViewMode)

                ;
    }

    public void apply() {
        // ----------------------------------- 序列化
        SerializationState serializationState = SerializationSettings.getInstance().getSerializationState();
        serializationState.setSerializeRandomValues(serializeRandomValuesCheckBox.isSelected());
        serializationState.setDetectFastJsonAnnotations(detectFastJsonAnnotationsCheckBox.isSelected());
        serializationState.setDetectJacksonAnnotations(detectJacksonAnnotationsCheckBox.isSelected());

        // ----------------------------------- 行为
        ToolWindowSettings toolWindowSettings = ToolWindowSettings.getInstance();
        EditorBehaviorState editorBehaviorState = toolWindowSettings.getBehaviorState();
        editorBehaviorState.setAutoRecognizeFormats(autoRecognizeFormatsCheckBox.isSelected());
        Set<DataFormatType> enabledFormats = editorBehaviorState.getEnabledFormats();
        if (enableXmlFormatsCheckBox.isSelected()) enabledFormats.add(DataFormatType.XML);
        if (enableYamlFormatsCheckBox.isSelected()) enabledFormats.add(DataFormatType.YAML);
        if (enableTomlFormatsCheckBox.isSelected()) enabledFormats.add(DataFormatType.TOML);
        if (enableTypeScriptFormatsCheckBox.isSelected()) enabledFormats.add(DataFormatType.TYPE_SCRIPT);
        if (enableUrlParamFormatsCheckBox.isSelected()) enabledFormats.add(DataFormatType.URL_PARAM);

        boolean oldShouldApplyToSource = editorBehaviorState.isShouldApplyToSource();
        editorBehaviorState.setShouldApplyToSource(applyToSourceCheckBox.isSelected());

        // ----------------------------------- 历史记录
        HistoryState historyState = toolWindowSettings.getHistoryState();
        boolean oldEnableHistory = historyState.isEnableHistory();

        historyState.setEnableHistory(enableHistoryCheckBox.isSelected());
        historyState.setAutoRecordHistory(autoRecordHistoryRadioBtn.isSelected());

        // ----------------------------------- 外观
        EditorVisualState visualState = toolWindowSettings.getVisualState();
        boolean oldShowLineNumbers = visualState.isShowLineNumbers();
        boolean oldShowFoldingOutline = visualState.isShowFoldingOutline();
        ColorScheme oldColorScheme = visualState.getColorScheme();

        visualState.setShowLineNumbers(showLineNumbersCheckBox.isSelected());
        visualState.setShowFoldingOutline(showFoldingOutlineCheckBox.isSelected());
        if (isIdea) visualState.setColorScheme(backgroundComboBox.getItem());

        // 常规
        TreeStructureState treeStructureState = GeneralSettings.getInstance().getState().getTreeStructureState();
        treeStructureState.setTreeViewMode(treeViewModeComboBox.getItem());

        // 发布配置更新事件
        fireConfigurationUpdateEvent(oldShowLineNumbers, oldShowFoldingOutline, oldColorScheme, oldEnableHistory, oldShouldApplyToSource);
    }

    /**
     * 针对性地发布配置更新事件
     */
    private void fireConfigurationUpdateEvent(boolean oldShowLineNumbers,
                                              boolean oldShowFoldingOutline,
                                              ColorScheme oldColorScheme,
                                              boolean oldEnableHistory,
                                              boolean oldShouldApplyToSource) {

        boolean newShowLineNumbers = showLineNumbersCheckBox.isSelected();
        boolean newShowFoldingOutline = showFoldingOutlineCheckBox.isSelected();
        ColorScheme newColorScheme = backgroundComboBox.getItem();
        boolean newEnableHistory = enableHistoryCheckBox.isSelected();
        boolean newShouldApplyToSource = applyToSourceCheckBox.isSelected();

        boolean showLineNumbersUpdate = !Objects.equals(oldShowLineNumbers, newShowLineNumbers);
        boolean showFoldingOutlineUpdate = !Objects.equals(oldShowFoldingOutline, newShowFoldingOutline);
        boolean colorSchemeUpdate = !Objects.equals(oldColorScheme, newColorScheme);
        boolean enableHistoryUpdate = !Objects.equals(oldEnableHistory, newEnableHistory);
        boolean shouldApplyToSourceUpdate = !Objects.equals(oldShouldApplyToSource, newShouldApplyToSource);


        // 对比新旧配置，针对性的进行事件发布
        if (showLineNumbersUpdate
                || showFoldingOutlineUpdate
                || (isIdea && colorSchemeUpdate)
                || enableHistoryUpdate
                || shouldApplyToSourceUpdate) {

            MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();

            // 切换展示行号事件
            if (showLineNumbersUpdate) {
                messageBus.syncPublisher(LineNumbersToggleEvent.TOPIC).toggle(newShowLineNumbers);
            }

            // 切换展示折叠轮廓事件
            if (showFoldingOutlineUpdate) {
                messageBus.syncPublisher(FoldingOutlineToggleEvent.TOPIC).toggle(newShowFoldingOutline);
            }

            // 切换编辑器背景色事件
            if (isIdea && colorSchemeUpdate) {
                messageBus.syncPublisher(ColorSchemeChangedEvent.TOPIC).change(newColorScheme);
            }

            if (enableHistoryUpdate) {
                messageBus.syncPublisher(HistoryEnabledEvent.TOPIC).enable(newEnableHistory);
            }

            if (shouldApplyToSourceUpdate) {
                // 使编辑器切换到 源文件/安全 模式
                messageBus.syncPublisher(ApplyToSourceToggleEvent.TOPIC).apply(newShouldApplyToSource);
            }
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(18, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(10, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        serializationTitle = new TitledSeparator();
        panel2.add(serializationTitle, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(16, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 2, new Insets(4, 17, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        serializeRandomValuesCheckBox = new JBCheckBox();
        panel3.add(serializeRandomValuesCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        serializeRandomValuesDesc = new JBLabel();
        panel3.add(serializeRandomValuesDesc, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 2, new Insets(0, 17, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        detectFastJsonAnnotationsCheckBox = new JBCheckBox();
        panel4.add(detectFastJsonAnnotationsCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel4.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        detectFastJsonAnnotationsDesc = new JBLabel();
        panel4.add(detectFastJsonAnnotationsDesc, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 2, new Insets(0, 17, 0, 0), -1, -1));
        panel1.add(panel5, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        detectJacksonAnnotationsCheckBox = new JBCheckBox();
        panel5.add(detectJacksonAnnotationsCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel5.add(spacer4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        detectJacksonAnnotationsDesc = new JBLabel();
        panel5.add(detectJacksonAnnotationsDesc, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(5, 0, 4, 0), -1, -1));
        panel1.add(panel6, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        editorBehaviorTitle = new TitledSeparator();
        panel6.add(editorBehaviorTitle, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 4, new Insets(0, 17, 0, 0), -1, -1));
        panel1.add(panel7, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel8, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel8.add(spacer5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        autoRecognizeFormatsCheckBox = new JBCheckBox();
        panel8.add(autoRecognizeFormatsCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        autoRecognizeFormatsDesc = new JBLabel();
        panel8.add(autoRecognizeFormatsDesc, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        formatsCheckBoxPanel = new JPanel();
        formatsCheckBoxPanel.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(formatsCheckBoxPanel, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        enableXmlFormatsCheckBox = new JBCheckBox();
        formatsCheckBoxPanel.add(enableXmlFormatsCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        formatsCheckBoxPanel.add(spacer6, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        enableYamlFormatsCheckBox = new JBCheckBox();
        formatsCheckBoxPanel.add(enableYamlFormatsCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 1, false));
        enableTomlFormatsCheckBox = new JBCheckBox();
        formatsCheckBoxPanel.add(enableTomlFormatsCheckBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 1, false));
        enableTypeScriptFormatsCheckBox = new JBCheckBox();
        formatsCheckBoxPanel.add(enableTypeScriptFormatsCheckBox, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 1, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 2, new Insets(0, 17, 0, 0), -1, -1));
        panel1.add(panel9, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        showLineNumbersCheckBox = new JBCheckBox();
        panel9.add(showLineNumbersCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel9.add(spacer7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 2, new Insets(0, 17, 0, 0), -1, -1));
        panel1.add(panel10, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        showFoldingOutlineCheckBox = new JBCheckBox();
        panel10.add(showFoldingOutlineCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        panel10.add(spacer8, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 2, new Insets(20, 10, 0, 0), -1, -1));
        panel1.add(panel11, new GridConstraints(17, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        donateLink = new ActionLink();
        panel11.add(donateLink, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        panel11.add(spacer9, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        backgroundPanel = new JPanel();
        backgroundPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 20, 0, 0), -1, -1));
        panel1.add(backgroundPanel, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        backgroundLabel = new JBLabel();
        backgroundPanel.add(backgroundLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        backgroundComboBox = new ComboBox();
        backgroundPanel.add(backgroundComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer10 = new Spacer();
        backgroundPanel.add(spacer10, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 1, new Insets(5, 0, 4, 0), -1, -1));
        panel1.add(panel12, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        editorVisualTitle = new TitledSeparator();
        panel12.add(editorVisualTitle, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(1, 1, new Insets(8, 0, 4, 0), -1, -1));
        panel1.add(panel13, new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        historyTitle = new TitledSeparator();
        panel13.add(historyTitle, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 4, 0), -1, -1));
        panel1.add(panel14, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        generalTitle = new TitledSeparator();
        panel14.add(generalTitle, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridLayoutManager(1, 4, new Insets(0, 17, 0, 0), -1, -1));
        panel1.add(panel15, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        treeViewModeLabel = new JBLabel();
        panel15.add(treeViewModeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer11 = new Spacer();
        panel15.add(spacer11, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        treeViewModeComboBox = new ComboBox();
        panel15.add(treeViewModeComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        treeViewModeDesc = new JBLabel();
        panel15.add(treeViewModeDesc, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new GridLayoutManager(1, 2, new Insets(0, 17, 0, 0), -1, -1));
        panel1.add(panel16, new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        enableHistoryCheckBox = new JBCheckBox();
        panel16.add(enableHistoryCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer12 = new Spacer();
        panel16.add(spacer12, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel17 = new JPanel();
        panel17.setLayout(new GridLayoutManager(1, 5, new Insets(4, 20, 2, 0), -1, -1));
        panel1.add(panel17, new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer13 = new Spacer();
        panel17.add(spacer13, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        autoRecordHistoryLabel = new JBLabel();
        panel17.add(autoRecordHistoryLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        autoRecordHistoryRadioBtn = new JBRadioButton();
        panel17.add(autoRecordHistoryRadioBtn, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        manualRecordHistoryRadioBtn = new JBRadioButton();
        panel17.add(manualRecordHistoryRadioBtn, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 1, false));
        autoRecordHistoryDesc = new JBLabel();
        panel17.add(autoRecordHistoryDesc, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel18 = new JPanel();
        panel18.setLayout(new GridLayoutManager(2, 2, new Insets(0, 17, 0, 0), -1, -1));
        panel1.add(panel18, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        applyToSourceCheckBox = new JBCheckBox();
        panel18.add(applyToSourceCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer14 = new Spacer();
        panel18.add(spacer14, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        applyToSourceDesc = new JBLabel();
        panel18.add(applyToSourceDesc, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
