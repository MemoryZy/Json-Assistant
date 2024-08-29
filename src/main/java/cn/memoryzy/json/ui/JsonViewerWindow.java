package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.actions.child.toolwindow.*;
import cn.memoryzy.json.extensions.JsonViewerEditorFloatingProvider;
import cn.memoryzy.json.models.LimitedList;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.ui.basic.JsonViewerPanel;
import cn.memoryzy.json.ui.basic.editor.FoldingLanguageTextEditor;
import cn.memoryzy.json.utils.JsonUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.tools.SimpleActionGroup;
import com.intellij.ui.LanguageTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class JsonViewerWindow {

    private LanguageTextField jsonTextField;
    private final Project project;
    private final boolean initWindow;
    private final JsonViewerHistoryState historyState;

    public JsonViewerWindow(Project project, boolean initWindow) {
        this.project = project;
        this.initWindow = initWindow;
        this.historyState = JsonViewerHistoryState.getInstance(project);
    }

    public JComponent getRootPanel() {
        this.jsonTextField = new FoldingLanguageTextEditor(JsonLanguage.INSTANCE, project, "");
        this.jsonTextField.setFont(new Font("Consolas", Font.PLAIN, 15));
        Document document = this.jsonTextField.getDocument();
        document.addDocumentListener(new JsonViewerEditorFloatingProvider.DocumentListenerImpl(project));
        this.jsonTextField.addFocusListener(new FocusListenerImpl());
        this.initJsonText();

        JsonViewerPanel rootPanel = new JsonViewerPanel(new BorderLayout(), this.jsonTextField);
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(jsonTextField, BorderLayout.CENTER);
        rootPanel.add(centerPanel, BorderLayout.CENTER);

        SimpleToolWindowPanel simpleToolWindowPanel = new SimpleToolWindowPanel(false, false);
        simpleToolWindowPanel.setContent(rootPanel);
        simpleToolWindowPanel.setToolbar(createToolbar(simpleToolWindowPanel));
        return simpleToolWindowPanel;
    }

    public JComponent createToolbar(SimpleToolWindowPanel simpleToolWindowPanel) {
        SimpleActionGroup actionGroup = new SimpleActionGroup();
        actionGroup.add(new JsonBeautifyToolWindowAction(this, simpleToolWindowPanel));
        actionGroup.add(new JsonMinifyToolWindowAction(this, simpleToolWindowPanel));
        actionGroup.add(Separator.create());
        actionGroup.add(new JsonStructureToolWindowAction(this, simpleToolWindowPanel));
        actionGroup.add(new JsonPathFilterOnTextFieldAction(this));
        actionGroup.add(Separator.create());
        actionGroup.add(new SaveToDiskAction(this));
        actionGroup.add(new ClearEditorAction(this));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_TOOLBAR_BAR, actionGroup, false);
        return toolbar.getComponent();
    }

    private void initJsonText() {
        if (initWindow) {
            String jsonStr = "";
            String clipboard = PlatformUtil.getClipboard();
            if (StrUtil.isNotBlank(clipboard)) {
                jsonStr = (JsonUtil.isJsonStr(clipboard)) ? clipboard : JsonUtil.extractJsonStr(clipboard);
            }

            if (StrUtil.isNotBlank(jsonStr)) {
                jsonTextField.setText(jsonStr);
            }
        }
    }

    public String getJsonContent() {
        return jsonTextField.getText();
    }

    public LanguageTextField getJsonTextField() {
        return jsonTextField;
    }


    private class FocusListenerImpl extends FocusAdapter {

        @Override
        public void focusGained(FocusEvent event) {
            if (initWindow) {
                String text = jsonTextField.getText();
                if (StrUtil.isBlank(text)) {
                    String clipboard = PlatformUtil.getClipboard();
                    if (StrUtil.isNotBlank(clipboard)) {
                        String jsonStr = (JsonUtil.isJsonStr(clipboard)) ? clipboard : JsonUtil.extractJsonStr(clipboard);
                        if (StrUtil.isNotBlank(jsonStr)) {
                            jsonStr = JsonUtil.formatJson(jsonStr);
                            jsonTextField.setText(jsonStr);
                        }
                    }
                }
            }
        }

        @Override
        public void focusLost(FocusEvent event) {
            LimitedList<String> historyList = historyState.getHistory();
            String text = StrUtil.trim(jsonTextField.getText());

            if (JsonUtil.isJsonStr(text)) {
                historyList.add(text);
            }
        }
    }

}
