package cn.memoryzy.json.actions.child;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.icons.AllIcons;
import com.intellij.jsonpath.JsonPathFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;
import com.jayway.jsonpath.JsonPath;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonPathFilterOnTextFieldAction extends DumbAwareAction {

    private static final Logger LOG = Logger.getInstance(JsonPathFilterOnTextFieldAction.class);

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
        EditorTextField jsonPathTextField = getEditorTextField(project);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(jsonPathTextField, BorderLayout.NORTH);
        rootPanel.setBorder(JBUI.Borders.empty(2));
        rootPanel.setPreferredSize(new Dimension(200, 35));

        JBPopupFactory.getInstance()
                .createComponentPopupBuilder(rootPanel, jsonPathTextField)
                .setFocusable(true)
                .setTitle(JsonAssistantBundle.messageOnSystem("popup.json.path.filter.on.text.field.title"))
                .setCancelButton(new IconButton(JsonAssistantBundle.messageOnSystem("popup.json.path.filter.cancel.btn.tooltip"), AllIcons.Actions.Cancel))
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
                .createPopup()
                .show(relativePoint);
    }

    private @NotNull EditorTextField getEditorTextField(Project project) {
        EditorTextField jsonPathTextField = new EditorTextField(project, JsonPathFileType.INSTANCE);
        jsonPathTextField.setFont(new Font("Consolas", Font.PLAIN, 15));
        Runnable runnable = () -> matchJsonPath(jsonPathTextField.getText());
        jsonPathTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                runnable.run();
            }
        });

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
            jsonTextField.setText(Objects.toString(result));
        } catch (Exception ex) {
            LOG.warn("JSONPath resolution failed", ex);
        }
    }
}
