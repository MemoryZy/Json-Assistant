package cn.memoryzy.json.actions.child;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.ui.extension.SearchExtension;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.jayway.jsonpath.JsonPath;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.util.List;
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
        Component source = (Component) e.getInputEvent().getSource();
        RelativePoint relativePoint = new RelativePoint(source, new Point(-(source.getWidth() * 2), source.getHeight() + 1));

        ExtendableTextField extendableTextField = new ExtendableTextField(20);
        extendableTextField.addExtension(new SearchExtension());
        Document document = extendableTextField.getDocument();
        document.addDocumentListener(new DocumentListenerImpl(project));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(extendableTextField, BorderLayout.CENTER);

        JBPopupFactory.getInstance()
                .createComponentPopupBuilder(panel, extendableTextField)
                .setFocusable(true)
                .setTitle(JsonAssistantBundle.messageOnSystem("popup.json.path.filter.on.text.field.title"))
                .setShowShadow(true)
                .setShowBorder(true)
                .setModalContext(false)
                .setLocateWithinScreenBounds(true)
                .setFocusable(true)
                .setRequestFocus(true)
                .setModalContext(false)
                .setCancelOnClickOutside(true)
                .setCancelOnOtherWindowOpen(true)
                .setCancelKeyEnabled(true)
                .setMovable(true)
                .createPopup()
                .show(relativePoint);
    }


    public void matchJsonPath(String jsonPath, Project project, boolean isRemove) {
        LanguageTextField jsonTextField = window.getJsonTextField();
        String json = jsonTextField.getText();
        if (Objects.isNull(jsonPath)) {
            return;
        }

        JsonViewerHistoryState historyState = JsonViewerHistoryState.getInstance(project);
        if ((StrUtil.isBlank(jsonPath) || Objects.equals("$", jsonPath)) && isRemove) {
            // 恢复上一次的完整记录
            List<String> historyList = historyState.getHistoryList();
            if (CollUtil.isNotEmpty(historyList)) {
                jsonTextField.setText(historyList.get(historyList.size() - 1));
                return;
            }
        }

        if (StrUtil.isBlank(json) || !JsonUtil.isJsonStr(json)) {
            return;
        }

        try {
            String result = JsonPath.read(json, jsonPath);
            jsonTextField.setText(result);
        } catch (Exception ignored) {
        }
    }


    public class DocumentListenerImpl implements DocumentListener {

        private final Project project;

        public DocumentListenerImpl(Project project) {
            this.project = project;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            matchJsonPath(getWholeText(e), project, false);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            matchJsonPath(getWholeText(e), project, true);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {

        }

        public String getWholeText(DocumentEvent e) {
            Document document = e.getDocument();
            String wholeText;
            try {
                wholeText = document.getText(0, document.getLength());
            } catch (BadLocationException ex) {
                LOG.error("Failed to obtain the input field text!", ex);
                return null;
            }

            return wholeText;
        }
    }

}
