package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.JsonAssistantPlugin;
import cn.memoryzy.json.ui.basic.CustomizedLanguageTextEditor;
import cn.memoryzy.json.ui.extension.SearchExtension;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.JBColor;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.fields.ExtendableTextField;
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
    public static final String JSON_PATH_LANGUAGE_CLASS_NAME = "com.intellij.jsonpath.JsonPathLanguage";
    private static final Class<?> JSON_PATH_LANGUAGE_CLASS = JsonAssistantUtil.getClass(JSON_PATH_LANGUAGE_CLASS_NAME);
    public static final String JSON_PATH_HISTORY_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonPathHistory";

    private final JComponent jsonPathTextField;
    private final CustomizedLanguageTextEditor showTextEditor;
    private final Runnable action;
    private final Project project;

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


    private @NotNull JComponent createJsonPathTextField(Project project, Runnable action) {
        JComponent jsonPathTextField;
        Font font = new Font("Microsoft YaHei UI", Font.PLAIN, 13);

        if (JSON_PATH_LANGUAGE_CLASS != null) {
            Language language = PlainTextLanguage.INSTANCE;
            Object instance = JsonAssistantUtil.getStaticFinalFieldValue(JSON_PATH_LANGUAGE_CLASS, "INSTANCE");
            if (instance instanceof Language) {
                language = (Language) instance;
            }

            LanguageTextField languageTextField = new LanguageTextField(language, project, "");
            languageTextField.setFont(font);
            languageTextField.setPlaceholder(JsonAssistantBundle.messageOnSystem("dialog.json.path.text.field.placeholder"));
            jsonPathTextField = languageTextField;
        } else {
            ExtendableTextField extendableTextField = new ExtendableTextField(20);
            extendableTextField.setFont(font);
            extendableTextField.addExtension(new SearchExtension(action));
            extendableTextField.getEmptyText().setText(JsonAssistantBundle.messageOnSystem("dialog.json.path.text.field.placeholder"));
            jsonPathTextField = extendableTextField;
        }

        return jsonPathTextField;
    }

    public void setJsonPathResult(String jsonStr) {
        String jsonPath;
        if (jsonPathTextField instanceof LanguageTextField) {
            jsonPath = ((LanguageTextField) jsonPathTextField).getText();
        } else {
            jsonPath = ((ExtendableTextField) jsonPathTextField).getText();
        }

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
            addJsonPathHistory(project, jsonStr);
        } catch (Exception ex) {
            LOG.warn("JSONPath resolution failed", ex);
        }
    }

    public void searchHistory(boolean isDown) {
        List<String> jsonPathHistoryList = getJsonPathHistoryList(project);



    }

    public JComponent getJsonPathTextField() {
        return jsonPathTextField;
    }

    public Runnable getAction() {
        return action;
    }


    public static List<String> getJsonPathHistoryList(Project project) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        String jsonPathHistoryStr = propertiesComponent.getValue(JSON_PATH_HISTORY_KEY);
        if (StrUtil.isNotBlank(jsonPathHistoryStr)) {
            return StrUtil.split(jsonPathHistoryStr, '\n');
        }

        return new ArrayList<>();
    }

    public static void addJsonPathHistory(Project project, String jsonPathStr) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        String jsonPathHistoryStr = propertiesComponent.getValue(JSON_PATH_HISTORY_KEY);
        List<String> jsonPathHistoryList = StrUtil.isNotBlank(jsonPathHistoryStr) ? StrUtil.split(jsonPathHistoryStr, '\n') : new ArrayList<>();

        if (!jsonPathHistoryList.contains(jsonPathStr)) {
            jsonPathHistoryList.add(StrUtil.trim(jsonPathStr));
            propertiesComponent.setValue(JSON_PATH_HISTORY_KEY, StrUtil.join("\n", jsonPathHistoryStr));
        }
    }

}
