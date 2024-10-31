package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.AttributeSerializationPersistentState;
import cn.memoryzy.json.service.persistent.EditorOptionsPersistentState;
import cn.memoryzy.json.ui.dialog.SupportDialog;
import com.intellij.icons.AllIcons;
import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.ui.panel.ComponentPanelBuilder;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
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
    private TitledSeparator toolWindowLabel;
    private JBCheckBox loadLastRecordCb;
    private JBLabel loadLastRecordDesc;
    private JBCheckBox recognizeOtherFormatsCb;
    private JBLabel recognizeOtherFormatsDesc;
    private JBCheckBox followEditorThemeCb;
    private JBCheckBox displayLineNumbersCb;
    private JBCheckBox foldingOutlineCb;
    private ActionLink donateLink;
    private JBLabel recognizeOtherFormatsTip;
    // endregion

    private final EditorOptionsPersistentState editorOptionsState = EditorOptionsPersistentState.getInstance();
    private final AttributeSerializationPersistentState attributeSerializationState = AttributeSerializationPersistentState.getInstance();

    public JPanel createRootPanel() {
        applyAttributeSerializationChunk();
        applyToolWindowChunk();
        applyDonateLinkChunk();

        // 初始化
        reset();

        return rootPanel;
    }

    private void applyAttributeSerializationChunk() {
        attributeSerializationLabel.setText(JsonAssistantBundle.messageOnSystem("setting.component.attribute.serialization.text"));

        includeRandomValuesCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.random.value.text"));
        setCommentLabel(includeRandomValuesDesc, includeRandomValuesCb, JsonAssistantBundle.messageOnSystem("setting.component.random.value.desc"));

        fastJsonCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.fastjson.text"));
        setCommentLabel(fastJsonDesc, fastJsonCb, JsonAssistantBundle.messageOnSystem("setting.component.fastjson.desc"));

        jacksonCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.jackson.text"));
        setCommentLabel(jacksonDesc, jacksonCb, JsonAssistantBundle.messageOnSystem("setting.component.jackson.desc"));
    }

    private void applyToolWindowChunk() {
        toolWindowLabel.setText(JsonAssistantBundle.messageOnSystem("setting.component.tool.window.text"));

        loadLastRecordCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.load.last.record.text"));
        setCommentLabel(loadLastRecordDesc, loadLastRecordCb, JsonAssistantBundle.messageOnSystem("setting.component.load.last.record.desc"));

        recognizeOtherFormatsTip.setIcon(AllIcons.General.ContextHelp);
        recognizeOtherFormatsCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.recognize.other.formats.text"));
        new HelpTooltip().setDescription(JsonAssistantBundle.messageOnSystem("setting.component.recognize.other.formats.tip")).installOn(recognizeOtherFormatsTip);
        setCommentLabel(recognizeOtherFormatsDesc, recognizeOtherFormatsCb, JsonAssistantBundle.messageOnSystem("setting.component.recognize.other.formats.desc"));

        followEditorThemeCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.follow.editor.theme.text"));
        displayLineNumbersCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.display.lines.text"));
        foldingOutlineCb.setText(JsonAssistantBundle.messageOnSystem("setting.component.folding.outline.text"));
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
        // 恢复为初始状态
        includeRandomValuesCb.setSelected(attributeSerializationState.includeRandomValues);
        fastJsonCb.setSelected(attributeSerializationState.recognitionFastJsonAnnotation);
        jacksonCb.setSelected(attributeSerializationState.recognitionJacksonAnnotation);

        loadLastRecordCb.setSelected(editorOptionsState.loadLastRecord);
        recognizeOtherFormatsCb.setSelected(editorOptionsState.recognizeOtherFormats);
        followEditorThemeCb.setSelected(editorOptionsState.followEditorTheme);
        displayLineNumbersCb.setSelected(editorOptionsState.displayLineNumbers);
        foldingOutlineCb.setSelected(editorOptionsState.foldingOutline);
    }


    public boolean isModified() {
        boolean oldIncludeRandomValues = attributeSerializationState.includeRandomValues;
        boolean oldRecognitionFastJsonAnnotation = attributeSerializationState.recognitionFastJsonAnnotation;
        boolean oldRecognitionJacksonAnnotation = attributeSerializationState.recognitionJacksonAnnotation;

        boolean oldLoadLastRecord = editorOptionsState.loadLastRecord;
        boolean oldRecognizeOtherFormats = editorOptionsState.recognizeOtherFormats;
        boolean oldFollowEditorTheme = editorOptionsState.followEditorTheme;
        boolean oldDisplayLineNumbers = editorOptionsState.displayLineNumbers;
        boolean oldFoldingOutline = editorOptionsState.foldingOutline;

        boolean includeRandomValues = includeRandomValuesCb.isSelected();
        boolean recognitionFastJsonAnnotation = fastJsonCb.isSelected();
        boolean recognitionJacksonAnnotation = jacksonCb.isSelected();

        boolean loadLastRecord = loadLastRecordCb.isSelected();
        boolean recognizeOtherFormats = recognizeOtherFormatsCb.isSelected();
        boolean followEditorTheme = followEditorThemeCb.isSelected();
        boolean displayLineNumbers = displayLineNumbersCb.isSelected();
        boolean foldingOutline = foldingOutlineCb.isSelected();

        return !Objects.equals(oldIncludeRandomValues, includeRandomValues)
                || !Objects.equals(oldRecognitionFastJsonAnnotation, recognitionFastJsonAnnotation)
                || !Objects.equals(oldRecognitionJacksonAnnotation, recognitionJacksonAnnotation)

                || !Objects.equals(oldLoadLastRecord, loadLastRecord)
                || !Objects.equals(oldRecognizeOtherFormats, recognizeOtherFormats)
                || !Objects.equals(oldFollowEditorTheme, followEditorTheme)
                || !Objects.equals(oldDisplayLineNumbers, displayLineNumbers)
                || !Objects.equals(oldFoldingOutline, foldingOutline);
    }

    public void apply() {
        attributeSerializationState.includeRandomValues = includeRandomValuesCb.isSelected();
        attributeSerializationState.recognitionFastJsonAnnotation = fastJsonCb.isSelected();
        attributeSerializationState.recognitionJacksonAnnotation = jacksonCb.isSelected();

        editorOptionsState.loadLastRecord = loadLastRecordCb.isSelected();
        editorOptionsState.recognizeOtherFormats = recognizeOtherFormatsCb.isSelected();
        editorOptionsState.followEditorTheme = followEditorThemeCb.isSelected();
        editorOptionsState.displayLineNumbers = displayLineNumbersCb.isSelected();
        editorOptionsState.foldingOutline = foldingOutlineCb.isSelected();
    }


    private static void setCommentLabel(JLabel label, JCheckBox checkBox, String commentText) {
        label.setForeground(JBUI.CurrentTheme.ContextHelp.FOREGROUND);
        label.setFont(ComponentPanelBuilder.getCommentFont(label.getFont()));
        label.setBorder(getCommentBorder(checkBox));
        setCommentText(label, commentText, true, ComponentPanelBuilder.MAX_COMMENT_WIDTH);
    }

    @SuppressWarnings("SameParameterValue")
    private static void setCommentText(@NotNull JLabel component,
                                       @Nullable String commentText,
                                       boolean isCommentBelow,
                                       int maxLineLength) {
        if (commentText != null) {
            @NonNls String css = "<head><style type=\"text/css\">\n" +
                    "a, a:link {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.Foreground.ENABLED) + ";}\n" +
                    "a:visited {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.Foreground.VISITED) + ";}\n" +
                    "a:hover {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.Foreground.HOVERED) + ";}\n" +
                    "a:active {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.Foreground.PRESSED) + ";}\n" +
                    //"body {background-color:#" + ColorUtil.toHex(JBColor.YELLOW) + ";}\n" + // Left for visual debugging
                    "</style>\n</head>";
            HtmlChunk text = HtmlChunk.raw(commentText);
            if (maxLineLength > 0 && commentText.length() > maxLineLength && isCommentBelow) {
                int width = component.getFontMetrics(component.getFont()).stringWidth(commentText.substring(0, maxLineLength));
                text = text.wrapWith(HtmlChunk.div().attr("width", width));
            } else {
                text = text.wrapWith(HtmlChunk.div());
            }
            component.setText(new HtmlBuilder()
                    .append(HtmlChunk.raw(css))
                    .append(text.wrapWith("body"))
                    .wrapWith("html")
                    .toString());
        }
    }

    private static Border getCommentBorder(JCheckBox checkBox) {
        Insets insets = ComponentPanelBuilder.computeCommentInsets(checkBox, true);
        insets.bottom -= 4;
        return new JBEmptyBorder(insets);
    }

}
