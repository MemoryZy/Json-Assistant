package cn.memoryzy.json.ui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.JsonAssistantPlugin;
import cn.memoryzy.json.ui.basic.UIManager;
import cn.memoryzy.json.ui.basic.editor.CustomizedLanguageTextEditor;
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
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBColor;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author Memory
 * @since 2024/8/14
 */
public class JsonPathPanel {
    private static final Logger LOG = Logger.getInstance(JsonPathPanel.class);
    public static final String JSON_PATH_FILE_TYPE_CLASS_NAME = "com.intellij.jsonpath.JsonPathFileType";
    public static final Class<?> JSON_PATH_FILE_TYPE_CLASS = JsonAssistantUtil.getClass(JSON_PATH_FILE_TYPE_CLASS_NAME);
    public static final String JSON_PATH_HISTORY_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonPathHistory";

    private final ComboBox<String> pathExpressionComboBoxTextField;
    private final CustomizedLanguageTextEditor showTextEditor;
    private final Runnable action;
    private final Project project;
    private ComboBoxEditor jsonPathNestedComboBoxEditor;
    private CollectionComboBoxModel<String> comboBoxModel;

    public JsonPathPanel(Project project, LanguageTextField jsonTextField) {
        this.project = project;
        this.action = () -> fillJsonPathResult(jsonTextField.getText());
        this.pathExpressionComboBoxTextField = createJsonPathTextField(project);
        this.showTextEditor = new CustomizedLanguageTextEditor(JsonLanguage.INSTANCE, project, "", true);
        this.showTextEditor.setFont(JBUI.Fonts.create("Consolas", 14));
    }

    public JPanel getRootPanel() {
        JBLabel tipLabel = new JBLabel(JsonAssistantBundle.messageOnSystem("dialog.json.path.text.field.history.tip"));
        tipLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tipLabel.setForeground(JBColor.GRAY);
        tipLabel.setFont(JBUI.Fonts.smallFont());
        tipLabel.setBorder(JBUI.Borders.empty(0, 2));

        JPanel firstPanel = new JPanel(new BorderLayout());
        firstPanel.add(pathExpressionComboBoxTextField, BorderLayout.CENTER);
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
        return rootPanel;
    }


    private @NotNull ComboBox<String> createJsonPathTextField(Project project) {
        List<String> jsonPathHistoryList = getExpressionHistory();
        comboBoxModel = new CollectionComboBoxModel<>(jsonPathHistoryList);
        ComboBox<String> comboBox = new ComboBox<>(comboBoxModel);
        JBFont font = JBUI.Fonts.create("JetBrains Mono", 14);

        if (JSON_PATH_FILE_TYPE_CLASS != null) {
            jsonPathNestedComboBoxEditor = new JsonPathFileTypeComboBoxEditor(project, getJsonPathFileType(), font);
            EditorTextField editorTextField = ((JsonPathFileTypeComboBoxEditor) jsonPathNestedComboBoxEditor).getEditorComponent();
            UIManager.addRemoveErrorListener(editorTextField, comboBox);
        } else {
            jsonPathNestedComboBoxEditor = new JsonPathExtendableComboBoxEditor(action, font);
            ExtendableTextField editorTextField = ((JsonPathExtendableComboBoxEditor) jsonPathNestedComboBoxEditor).getEditorTextField();
            UIManager.addRemoveErrorListener(editorTextField, comboBox);
        }

        comboBox.setEditor(jsonPathNestedComboBoxEditor);
        comboBox.setEditable(true);
        jsonPathNestedComboBoxEditor.setItem(null);

        return comboBox;
    }

    public void fillJsonPathResult(String jsonStr) {
        String jsonPath = getPathExpression();
        if (Objects.isNull(jsonPath) || StrUtil.isBlank(jsonStr) || !JsonUtil.isJsonStr(jsonStr)) {
            return;
        }

        try {
            Object result = JsonPath.read(jsonStr, jsonPath);
            String jsonResult;
            if (result instanceof Map || result instanceof Iterable) {
                jsonResult = JsonUtil.formatJson(JSONUtil.toJsonStr(result));
            } else {
                if (result == null) {
                    jsonResult = "null";
                } else if (result instanceof String) {
                    jsonResult = "\"" + result + "\"";
                } else {
                    jsonResult = Objects.toString(result);
                }
            }

            showTextEditor.setText(jsonResult);

            // 添加至历史记录
            addJSONPathToHistory(jsonPath);
        } catch (Exception ex) {
            UIManager.addErrorBorder(pathExpressionComboBoxTextField);

            if (ex instanceof PathNotFoundException) {
                showTextEditor.setText(ex.getMessage());
                return;
            }

            LOG.warn("JSONPath resolution failed", ex);
        }
    }

    public void searchHistory(boolean isUp) {
        // 历史记录列表
        List<String> history = getExpressionHistory();
        if (CollUtil.isEmpty(history)) {
            return;
        }

        // 当前编辑框内的JsonPath
        String expression = getPathExpression();
        // 获取索引
        int index = history.indexOf(expression);
        // 清空选中
        pathExpressionComboBoxTextField.setSelectedItem(null);

        if (isUp) {
            // 从前往后
            if (index == -1) {
                pathExpressionComboBoxTextField.setSelectedIndex(0);
            } else {
                pathExpressionComboBoxTextField.setSelectedIndex((index < (history.size() - 1)) ? index + 1 : index);
            }
        } else {
            if (index != -1) {
                pathExpressionComboBoxTextField.setSelectedIndex((index == 0) ? 0 : index - 1);
            }
        }
    }

    public JComponent getPathExpressionComboBoxTextField() {
        return pathExpressionComboBoxTextField;
    }

    public Runnable getAction() {
        return action;
    }


    public List<String> getExpressionHistory() {
        String expressionHistory = PropertiesComponent.getInstance(project).getValue(JSON_PATH_HISTORY_KEY);
        return StrUtil.isNotBlank(expressionHistory) ? StrUtil.split(expressionHistory, '\n') : new ArrayList<>();
    }

    public void setExpressionHistory(Collection<String> history) {
        PropertiesComponent.getInstance(project).setValue(JSON_PATH_HISTORY_KEY, StrUtil.join("\n", history));
    }


    public void addJSONPathToHistory(String expression) {
        expression = StrUtil.trim(expression);

        ArrayDeque<String> history = new ArrayDeque<>(getExpressionHistory());
        if (!history.contains(expression)) {
            history.addFirst(expression);
            if (history.size() > 10) {
                history.removeLast();
            }

            setExpressionHistory(history);
            comboBoxModel.add(0, expression);
        } else {
            // 移动位置
            if (StrUtil.equals(history.getFirst(), expression)) {
                // 若已是第一个元素，则不需管
                return;
            }

            history.remove(expression);
            history.addFirst(expression);
            setExpressionHistory(history);

            comboBoxModel.remove(expression);
            comboBoxModel.add(0, expression);
        }

        setPathExpression(expression);
    }


    public String getPathExpression() {
        return StrUtil.trim((jsonPathNestedComboBoxEditor instanceof JsonPathFileTypeComboBoxEditor)
                ? ((JsonPathFileTypeComboBoxEditor) jsonPathNestedComboBoxEditor).getEditorText()
                : ((JsonPathExtendableComboBoxEditor) jsonPathNestedComboBoxEditor).getEditorText());
    }

    public void setPathExpression(String jsonPath) {
        if (jsonPathNestedComboBoxEditor instanceof JsonPathFileTypeComboBoxEditor) {
            ((JsonPathFileTypeComboBoxEditor) jsonPathNestedComboBoxEditor).setEditorText(jsonPath);
        } else {
            ((JsonPathExtendableComboBoxEditor) jsonPathNestedComboBoxEditor).setEditorText(jsonPath);
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
