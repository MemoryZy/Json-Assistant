package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.ColorScheme;
import cn.memoryzy.json.enums.DataFormatType;
import cn.memoryzy.json.enums.HistoryDisplayMode;
import cn.memoryzy.json.enums.TreeViewMode;
import cn.memoryzy.json.event.*;
import cn.memoryzy.json.service.persistent.state.v2.*;
import cn.memoryzy.json.service.persistent.v2.GeneralSettings;
import cn.memoryzy.json.service.persistent.v2.SerializationSettings;
import cn.memoryzy.json.service.persistent.v2.ToolWindowSettings;
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
    private JBCheckBox enableUrlParamFormatsCheckBox;
    private JPanel formatsCheckBoxPanel;

    private TitledSeparator editorVisualTitle;
    private JBLabel backgroundLabel;
    private ComboBox<ColorScheme> backgroundComboBox;
    private JPanel backgroundPanel;
    private JBLabel backgroundDesc;

    private TitledSeparator historyTitle;
    private JBCheckBox enableHistoryCheckBox;
    private JBLabel historyStyleLabel;
    private ComboBox<HistoryDisplayMode> historyStyleComboBox;

    private TitledSeparator generalTitle;
    private JBLabel treeViewModeLabel;
    private JBLabel treeViewModeDesc;
    private ComboBox<TreeViewMode> treeViewModeComboBox;
    private JBLabel autoRecordHistoryLabel;
    private JBRadioButton autoRecordHistoryRadioBtn;
    private JBRadioButton manualRecordHistoryRadioBtn;
    private JBLabel autoRecordHistoryDesc;

    private ActionLink donateLink;
    // endregion


    private final boolean isIdea = PlatformUtil.isIdea();


    public JPanel createComponent() {
        configureGeneralComponents();
        configureAttributeSerializationComponents();
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
    private void configureAttributeSerializationComponents() {
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
        enableUrlParamFormatsCheckBox.setText("URL Param");

        int left = UIUtil.getCheckBoxTextHorizontalOffset(autoRecognizeFormatsCheckBox);
        formatsCheckBoxPanel.setBorder(new JBEmptyBorder(JBUI.insets(1, left, 4, 0)));

        autoRecognizeFormatsCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                UIUtils.controlEnableCheckBox(enableXmlFormatsCheckBox, true);
                UIUtils.controlEnableCheckBox(enableYamlFormatsCheckBox, true);
                UIUtils.controlEnableCheckBox(enableTomlFormatsCheckBox, true);
                UIUtils.controlEnableCheckBox(enableUrlParamFormatsCheckBox, true);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                UIUtils.controlEnableCheckBox(enableXmlFormatsCheckBox, false);
                UIUtils.controlEnableCheckBox(enableYamlFormatsCheckBox, false);
                UIUtils.controlEnableCheckBox(enableTomlFormatsCheckBox, false);
                UIUtils.controlEnableCheckBox(enableUrlParamFormatsCheckBox, false);
            }
        });
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

            UIUtils.setHelpLabel(backgroundDesc, JsonAssistantBundle.messageOnSystem("setting.component.background.color.desc"));

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
        historyStyleLabel.setText(JsonAssistantBundle.messageOnSystem("setting.component.history.style.text"));
        for (HistoryDisplayMode value : HistoryDisplayMode.values()) {
            historyStyleComboBox.addItem(value);
        }

        historyStyleComboBox.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@NotNull JList<? extends HistoryDisplayMode> list, HistoryDisplayMode value, int index, boolean selected, boolean hasFocus) {
                setText(null != value ? JsonAssistantBundle.messageOnSystem(value.getKey()) : "");
            }
        });

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
                if (!historyStyleComboBox.isEnabled()) {
                    historyStyleComboBox.setEnabled(true);
                }
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                UIUtils.controlEnableRadioButton(autoRecordHistoryRadioBtn, false);
                UIUtils.controlEnableRadioButton(manualRecordHistoryRadioBtn, false);
                if (historyStyleComboBox.isEnabled()) {
                    historyStyleComboBox.setEnabled(false);
                }
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
        enableUrlParamFormatsCheckBox.setSelected(enabledFormats.contains(DataFormatType.URL_PARAM));

        // ----------------------------------- 外观
        EditorVisualState visualState = toolWindowSettings.getVisualState();
        showLineNumbersCheckBox.setSelected(visualState.isShowLineNumbers());
        showFoldingOutlineCheckBox.setSelected(visualState.isShowFoldingOutline());
        backgroundComboBox.setItem(visualState.getColorScheme());

        // ----------------------------------- 历史记录
        HistoryState historyState = toolWindowSettings.getHistoryState();
        boolean enableHistory = historyState.isEnableHistory();

        enableHistoryCheckBox.setSelected(enableHistory);
        historyStyleComboBox.setItem(historyState.getHistoryDisplayMode());
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
            if (!historyStyleComboBox.isEnabled()) {
                historyStyleComboBox.setEnabled(true);
            }
        } else {
            UIUtils.controlEnableRadioButton(autoRecordHistoryRadioBtn, false);
            UIUtils.controlEnableRadioButton(manualRecordHistoryRadioBtn, false);
            if (historyStyleComboBox.isEnabled()) {
                historyStyleComboBox.setEnabled(false);
            }
        }

        // ---------------------------- 解析格式
        if (autoRecognizeFormats) {
            UIUtils.controlEnableCheckBox(enableXmlFormatsCheckBox, true);
            UIUtils.controlEnableCheckBox(enableYamlFormatsCheckBox, true);
            UIUtils.controlEnableCheckBox(enableTomlFormatsCheckBox, true);
            UIUtils.controlEnableCheckBox(enableUrlParamFormatsCheckBox, true);
        } else {
            UIUtils.controlEnableCheckBox(enableXmlFormatsCheckBox, false);
            UIUtils.controlEnableCheckBox(enableYamlFormatsCheckBox, false);
            UIUtils.controlEnableCheckBox(enableTomlFormatsCheckBox, false);
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
        boolean oldEnableUrlParamFormat = enabledFormats.contains(DataFormatType.URL_PARAM);

        // ----------------------------------- 外观
        EditorVisualState visualState = toolWindowSettings.getVisualState();
        boolean oldShowLineNumbers = visualState.isShowLineNumbers();
        boolean oldShowFoldingOutline = visualState.isShowFoldingOutline();
        ColorScheme oldColorScheme = visualState.getColorScheme();

        // ----------------------------------- 历史记录
        HistoryState historyState = toolWindowSettings.getHistoryState();
        boolean oldEnableHistory = historyState.isEnableHistory();
        boolean oldAutoRecordHistory = historyState.isAutoRecordHistory();
        HistoryDisplayMode oldHistoryDisplayMode = historyState.getHistoryDisplayMode();

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
        boolean newEnableUrlParamFormat = enableUrlParamFormatsCheckBox.isSelected();

        // ----------------------------------- 历史记录
        boolean newEnableHistory = enableHistoryCheckBox.isSelected();
        boolean newAutoRecordHistory = autoRecordHistoryRadioBtn.isSelected();
        HistoryDisplayMode newHistoryDisplayMode = historyStyleComboBox.getItem();

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
                || !Objects.equals(oldEnableUrlParamFormat, newEnableUrlParamFormat)
                || !Objects.equals(oldEnableHistory, newEnableHistory)
                || !Objects.equals(oldAutoRecordHistory, newAutoRecordHistory)
                || !Objects.equals(oldHistoryDisplayMode, newHistoryDisplayMode)
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
        if (enableUrlParamFormatsCheckBox.isSelected()) enabledFormats.add(DataFormatType.URL_PARAM);

        // ----------------------------------- 历史记录
        HistoryState historyState = toolWindowSettings.getHistoryState();
        boolean oldEnableHistory = historyState.isEnableHistory();
        HistoryDisplayMode oldHistoryDisplayMode = historyState.getHistoryDisplayMode();

        historyState.setEnableHistory(enableHistoryCheckBox.isSelected());
        historyState.setAutoRecordHistory(autoRecordHistoryRadioBtn.isSelected());
        historyState.setHistoryDisplayMode(historyStyleComboBox.getItem());

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
        fireConfigurationUpdateEvent(oldShowLineNumbers, oldShowFoldingOutline, oldColorScheme, oldEnableHistory, oldHistoryDisplayMode);
    }

    /**
     * 针对性地发布配置更新事件
     */
    private void fireConfigurationUpdateEvent(boolean oldShowLineNumbers,
                                              boolean oldShowFoldingOutline,
                                              ColorScheme oldColorScheme,
                                              boolean oldEnableHistory,
                                              HistoryDisplayMode oldHistoryDisplayMode) {

        boolean newShowLineNumbers = showLineNumbersCheckBox.isSelected();
        boolean newShowFoldingOutline = showFoldingOutlineCheckBox.isSelected();
        ColorScheme newColorScheme = backgroundComboBox.getItem();
        boolean newEnableHistory = enableHistoryCheckBox.isSelected();
        HistoryDisplayMode newHistoryDisplayMode = historyStyleComboBox.getItem();

        boolean showLineNumbersUpdate = !Objects.equals(oldShowLineNumbers, newShowLineNumbers);
        boolean showFoldingOutlineUpdate = !Objects.equals(oldShowFoldingOutline, newShowFoldingOutline);
        boolean colorSchemeUpdate = !Objects.equals(oldColorScheme, newColorScheme);
        boolean enableHistoryUpdate = !Objects.equals(oldEnableHistory, newEnableHistory);
        boolean historyDisplayModeUpdate = !Objects.equals(oldHistoryDisplayMode, newHistoryDisplayMode);

        // 对比新旧配置，针对性的进行事件发布
        if (showLineNumbersUpdate || showFoldingOutlineUpdate || (isIdea && colorSchemeUpdate) || enableHistoryUpdate || historyDisplayModeUpdate) {
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

            if (historyDisplayModeUpdate) {
                messageBus.syncPublisher(HistoryViewChangedEvent.TOPIC).change(newHistoryDisplayMode);
            }
        }
    }

}
