package cn.memoryzy.json.actions.child;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.ui.extension.SearchExtension;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.icons.AllIcons;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.JBUI;
import com.jayway.jsonpath.JsonPath;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonPathFilterOnTextFieldAction extends DumbAwareAction implements UpdateInBackground {

    private static final Logger LOG = Logger.getInstance(JsonPathFilterOnTextFieldAction.class);
    public static final String TEXT_FIELD_PROPERTY_NAME = "JsonPathTextFieldAction";
    public static final String JSON_PATH_LANGUAGE_CLASS_NAME = "com.intellij.jsonpath.JsonPathLanguage";
    private static final Class<?> JSON_PATH_LANGUAGE_CLASS = JsonAssistantUtil.getClass(JSON_PATH_LANGUAGE_CLASS_NAME);

    private final JsonViewerWindow window;

    public JsonPathFilterOnTextFieldAction(JsonViewerWindow window) {
        super(JsonAssistantBundle.messageOnSystem("action.json.path.filter.text"),
                JsonAssistantBundle.messageOnSystem("action.json.path.filter.description"),
                JsonAssistantIcons.SEARCH);
        this.window = window;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        Component source = (Component) e.getInputEvent().getSource();
        RelativePoint relativePoint = new RelativePoint(source, new Point(-(source.getWidth() * 3), source.getHeight() + 1));
        JComponent jsonPathTextField = getJsonPathTextField(project);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(jsonPathTextField, BorderLayout.CENTER);
        rootPanel.setBorder(JBUI.Borders.empty(3));
        rootPanel.setPreferredSize(new Dimension(220, 35));

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(rootPanel, jsonPathTextField)
                .setFocusable(true)
                .setTitle(JsonAssistantBundle.messageOnSystem("popup.json.path.filter.on.text.field.title"))
                .setCancelButton(new IconButton(JsonAssistantBundle.messageOnSystem("popup.json.path.filter.cancel.btn.tooltip"), AllIcons.General.HideToolWindow))
                .setShowShadow(true)
                .setShowBorder(true)
                .setModalContext(false)
                .setLocateWithinScreenBounds(true)
                .setFocusable(true)
                .setRequestFocus(true)
                .setModalContext(false)
                .setCancelOnClickOutside(false)
                .setCancelOnOtherWindowOpen(false)
                .setCancelKeyEnabled(true)
                .setMovable(true)
                .createPopup();

        // 注册回车动作
        Runnable action = (Runnable) jsonPathTextField.getClientProperty(TEXT_FIELD_PROPERTY_NAME);
        new DumbAwareAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                action.run();
            }
        }.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke((char) KeyEvent.VK_ENTER)), jsonPathTextField, popup);

        // 弹出
        popup.show(relativePoint);
    }

    private @NotNull JComponent getJsonPathTextField(Project project) {
        JComponent jsonPathTextField;
        Runnable runnable;

        if (JSON_PATH_LANGUAGE_CLASS != null) {
            Language language = PlainTextLanguage.INSTANCE;
            Object instance = JsonAssistantUtil.getStaticFinalFieldValue(JSON_PATH_LANGUAGE_CLASS, "INSTANCE");
            if (instance instanceof Language) {
                language = (Language) instance;
            }

            jsonPathTextField = new LanguageTextField(language, project, "");
            jsonPathTextField.setFont(JBUI.Fonts.create("JetBrains Mono", 13));
            runnable = () -> matchJsonPath(((LanguageTextField) jsonPathTextField).getText());
        } else {
            jsonPathTextField = new ExtendableTextField(20);
            runnable = () -> matchJsonPath(((ExtendableTextField) jsonPathTextField).getText());
            ((ExtendableTextField) jsonPathTextField).addExtension(new SearchExtension(runnable));
        }

        jsonPathTextField.putClientProperty(TEXT_FIELD_PROPERTY_NAME, runnable);
        return jsonPathTextField;
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        String text = StrUtil.trim(window.getJsonContent());
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);
        e.getPresentation().setEnabled(StrUtil.isNotBlank(jsonStr));
    }


    public void matchJsonPath(String jsonPath) {
        LanguageTextField jsonTextField = window.getJsonTextField();
        String json = jsonTextField.getText();
        if (Objects.isNull(jsonPath) || StrUtil.isBlank(json) || !JsonUtil.isJsonStr(json)) {
            return;
        }

        try {
            Object result = JsonPath.read(json, jsonPath);
            String jsonResult;
            if (result instanceof Map || result instanceof List) {
                jsonResult = JsonUtil.formatJson(JSONUtil.toJsonStr(result));
            } else {
                jsonResult = Objects.toString(result);
            }

            jsonTextField.setText(jsonResult);
        } catch (Exception ex) {
            LOG.warn("JSONPath resolution failed", ex);
        }
    }

}
