package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.basic.CustomizedLanguageTextEditor;
import cn.memoryzy.json.ui.extension.SearchExtension;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.JBUI;
import com.jayway.jsonpath.JsonPath;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/14
 */
public class JsonPathDialog {
    private static final Logger LOG = Logger.getInstance(JsonPathDialog.class);
    public static final String TEXT_FIELD_PROPERTY_NAME = "JsonPathTextFieldAction";
    public static final String JSON_PATH_LANGUAGE_CLASS_NAME = "com.intellij.jsonpath.JsonPathLanguage";
    private static final Class<?> JSON_PATH_LANGUAGE_CLASS = JsonAssistantUtil.getClass(JSON_PATH_LANGUAGE_CLASS_NAME);

    private final JComponent jsonPathTextField;
    private final CustomizedLanguageTextEditor showTextEditor;
    private final Runnable action;

    public JsonPathDialog(Project project, LanguageTextField jsonTextField) {
        this.action = () -> setJsonPathResult(jsonTextField.getText());
        this.jsonPathTextField = createJsonPathTextField(project, action);
        this.jsonPathTextField.setFont(JBUI.Fonts.create("JetBrains Mono", 13));
        this.showTextEditor = new CustomizedLanguageTextEditor(JsonLanguage.INSTANCE, project, "", true);
        this.showTextEditor.setFont(JBUI.Fonts.create("Consolas", 14));
    }

    public JPanel getRootPanel() {
        JPanel secondPanel = new JPanel(new BorderLayout());
        JBLabel label = new JBLabel(JsonAssistantBundle.messageOnSystem("dialog.json.path.separate.label.text"));
        secondPanel.add(label, BorderLayout.NORTH);
        secondPanel.add(showTextEditor, BorderLayout.CENTER);

        Splitter splitter = new Splitter(true, 0.1f);
        splitter.setFirstComponent(jsonPathTextField);
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

        if (JSON_PATH_LANGUAGE_CLASS != null) {
            Language language = PlainTextLanguage.INSTANCE;
            Object instance = JsonAssistantUtil.getStaticFinalFieldValue(JSON_PATH_LANGUAGE_CLASS, "INSTANCE");
            if (instance instanceof Language) {
                language = (Language) instance;
            }

            jsonPathTextField = new LanguageTextField(language, project, "");
            jsonPathTextField.setFont(JBUI.Fonts.create("JetBrains Mono", 13));
        } else {
            jsonPathTextField = new ExtendableTextField(20);
            ((ExtendableTextField) jsonPathTextField).addExtension(new SearchExtension(action));
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
            String name = result.getClass().getName();
            String jsonResult;
            if (result instanceof Map || result instanceof Iterable) {
                jsonResult = JsonUtil.formatJson(JSONUtil.toJsonStr(result));
            } else {
                jsonResult = Objects.toString(result);
            }

            showTextEditor.setText(jsonResult);
        } catch (Exception ex) {
            LOG.warn("JSONPath resolution failed", ex);
        }
    }

    public JComponent getJsonPathTextField() {
        return jsonPathTextField;
    }

    public Runnable getAction() {
        return action;
    }

}
