package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.JsonAssistantPlugin;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.ui.basic.CustomizedLanguageTextEditor;
import cn.memoryzy.json.ui.basic.jsonpath.JsonPathExtendableComboBoxEditor;
import cn.memoryzy.json.ui.basic.jsonpath.JsonPathFileTypeComboBoxEditor;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.JBColor;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.jayway.jsonpath.JsonPath;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/14
 */
public class JsonPathPanel {
    private static final Logger LOG = Logger.getInstance(JsonPathPanel.class);
    public static final String TEXT_FIELD_PROPERTY_NAME = "JsonPathTextFieldAction";
    public static final String JSON_PATH_FILE_TYPE_CLASS_NAME = "com.intellij.jsonpath.JsonPathFileType";
    public static final Class<?> JSON_PATH_FILE_TYPE_CLASS = JsonAssistantUtil.getClass(JSON_PATH_FILE_TYPE_CLASS_NAME);
    public static final String JSON_PATH_HISTORY_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonPathHistory";

    private final ComboBox<String> jsonPathTextField;
    private final CustomizedLanguageTextEditor showTextEditor;
    private final Runnable action;
    private final Project project;
    private ComboBoxEditor editor;

    public JsonPathPanel(Project project, LanguageTextField jsonTextField) {
        this.project = project;
        this.action = () -> setJsonPathResult(jsonTextField.getText());
        this.jsonPathTextField = createJsonPathTextField(project, action);
        this.showTextEditor = new CustomizedLanguageTextEditor(JsonLanguage.INSTANCE, project, "", true);
        this.showTextEditor.setFont(JBUI.Fonts.create("Consolas", 14));
    }

    public JPanel getRootPanel() {
        JBLabel tipLabel = new JBLabel(JsonAssistantBundle.message("dialog.json.path.text.field.history.tip"));
        tipLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tipLabel.setForeground(JBColor.GRAY);
        tipLabel.setFont(JBUI.Fonts.label(11));
        tipLabel.setBorder(JBUI.Borders.empty(0, 2));

        JPanel firstPanel = new JPanel(new BorderLayout());
        firstPanel.add(jsonPathTextField, BorderLayout.CENTER);
        firstPanel.add(tipLabel, BorderLayout.SOUTH);

        JPanel secondPanel = new JPanel(new BorderLayout());
        JBLabel resultLabel = new JBLabel(" " + JsonAssistantBundle.messageOnSystem("dialog.json.path.separate.label.text"));
        secondPanel.add(resultLabel, BorderLayout.NORTH);
        secondPanel.add(showTextEditor, BorderLayout.CENTER);

        Splitter splitter = new Splitter(true, 0.1f);
        splitter.setFirstComponent(firstPanel);
        splitter.setSecondComponent(secondPanel);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(splitter, BorderLayout.CENTER);
        rootPanel.setPreferredSize(new Dimension(260, 280));
        rootPanel.setBorder(JBUI.Borders.empty(3));
        rootPanel.putClientProperty(TEXT_FIELD_PROPERTY_NAME, action);
        return rootPanel;
    }


    private @NotNull ComboBox<String> createJsonPathTextField(Project project, Runnable action) {
        JBFont font = JBUI.Fonts.label(13);
        editor = JSON_PATH_FILE_TYPE_CLASS != null
                ? new JsonPathFileTypeComboBoxEditor(project, getJsonPathFileType(), font)
                : new JsonPathExtendableComboBoxEditor(action, font);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setEditor(editor);
        comboBox.setEditable(true);

        for (String jsonPathHistory : getJsonPathHistoryList()) {
            comboBox.addItem(jsonPathHistory);
        }

        return comboBox;
    }

    public void setJsonPathResult(String jsonStr) {
        String jsonPath = (editor instanceof JsonPathFileTypeComboBoxEditor)
                ? ((JsonPathFileTypeComboBoxEditor) editor).getEditorText()
                : ((JsonPathExtendableComboBoxEditor) editor).getEditorText();

        if (Objects.isNull(jsonPath) || StrUtil.isBlank(jsonStr) || !JsonUtil.isJsonStr(jsonStr)) {
            return;
        }

        try {
            Object result = JsonPath.read(jsonStr, jsonPath);
            String jsonResult;
            if (result instanceof Map || result instanceof Iterable) {
                jsonResult = JsonUtil.formatJson(JSONUtil.toJsonStr(result));
            } else {
                jsonResult = Objects.toString(result);
            }

            showTextEditor.setText(jsonResult);

            // 添加至历史记录
            addJsonPathHistory(jsonPath);
        } catch (Exception ex) {
            LOG.warn("JSONPath resolution failed", ex);
        }
    }

    public void searchHistory(boolean isDown) {
        List<String> jsonPathHistoryList = getJsonPathHistoryList();


    }

    public JComponent getJsonPathTextField() {
        return jsonPathTextField;
    }

    public Runnable getAction() {
        return action;
    }


    public List<String> getJsonPathHistoryList() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        String jsonPathHistoryStr = propertiesComponent.getValue(JSON_PATH_HISTORY_KEY);
        if (StrUtil.isNotBlank(jsonPathHistoryStr)) {
            return StrUtil.split(jsonPathHistoryStr, '\n');
        }

        return new ArrayList<>();
    }

    public void addJsonPathHistory(String jsonPath) {
        jsonPath = StrUtil.trim(jsonPath);
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        String jsonPathHistoryStr = propertiesComponent.getValue(JSON_PATH_HISTORY_KEY);
        List<String> jsonPathHistoryList = StrUtil.isNotBlank(jsonPathHistoryStr) ? StrUtil.split(jsonPathHistoryStr, '\n') : new ArrayList<>();

        if (!jsonPathHistoryList.contains(jsonPath)) {
            // 限制为20个
            if (jsonPathHistoryList.size() == JsonViewerHistoryState.HISTORY_LIMIT) {
                jsonPathHistoryList.remove(0);
                jsonPathTextField.remove(0);
            }

            jsonPathHistoryList.add(jsonPath);
            jsonPathTextField.addItem(jsonPath);

            if (editor instanceof JsonPathFileTypeComboBoxEditor) {
                ((JsonPathFileTypeComboBoxEditor) editor).setEditorText(jsonPath);
            } else {
                ((JsonPathExtendableComboBoxEditor) editor).setEditorText(jsonPath);
            }

            propertiesComponent.setValue(JSON_PATH_HISTORY_KEY, StrUtil.join("\n", jsonPathHistoryList));
        }
    }

    public static FileType getJsonPathFileType() {
        FileType fileType = PlainTextFileType.INSTANCE;
        Object instance = JsonAssistantUtil.getStaticFinalFieldValue(JsonPathPanel.JSON_PATH_FILE_TYPE_CLASS, "INSTANCE");
        if (instance instanceof FileType) {
            fileType = (FileType) instance;
        }

        return fileType;
    }
}
