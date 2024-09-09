package cn.memoryzy.json.ui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.toolwindow.*;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.LimitedList;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.ui.component.JsonViewerPanel;
import cn.memoryzy.json.ui.component.editor.FoldingLanguageTextEditor;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.tools.SimpleActionGroup;
import com.intellij.ui.LanguageTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class JsonViewerWindow {

    private LanguageTextField jsonTextField;
    private final Project project;
    private final boolean initWindow;
    private final JsonViewerHistoryState historyState;

    /**
     * 上次光标所在记录
     */
    private int lastOffset = 0;

    /**
     * 编辑器折叠区域
     */
    private final List<FoldRegion> lastRegions = new ArrayList<>();

    public JsonViewerWindow(Project project, boolean initWindow) {
        this.project = project;
        this.initWindow = initWindow;
        this.historyState = JsonViewerHistoryState.getInstance(project);
    }

    public JComponent getRootPanel() {
        this.jsonTextField = new FoldingLanguageTextEditor(JsonLanguage.INSTANCE, project, "");
        this.jsonTextField.setFont(new Font("Consolas", Font.PLAIN, 15));
        this.jsonTextField.addFocusListener(new FocusListenerImpl());
        this.jsonTextField.setPlaceholder(JsonAssistantBundle.messageOnSystem("placeholder.json.viewer.text"));
        this.jsonTextField.setShowPlaceholderWhenFocused(true);
        this.initEditorText();

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
        actionGroup.add(new JsonBeautifyToolWindowAction(jsonTextField, simpleToolWindowPanel));
        actionGroup.add(new JsonMinifyToolWindowAction(jsonTextField, simpleToolWindowPanel));
        actionGroup.add(Separator.create());
        actionGroup.add(new JsonStructureToolWindowAction(jsonTextField, simpleToolWindowPanel));
        actionGroup.add(new JsonPathAction(jsonTextField, simpleToolWindowPanel));
        actionGroup.add(Separator.create());
        actionGroup.add(new SaveToDiskAction(jsonTextField));
        actionGroup.add(new ClearEditorAction(jsonTextField));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actionGroup, false);
        return toolbar.getComponent();
    }

    private void initEditorText() {
        if (initWindow) {
            String jsonStr = "";
            String clipboard = PlatformUtil.getClipboard();
            if (StrUtil.isNotBlank(clipboard)) {
                jsonStr = (JsonUtil.isJsonStr(clipboard)) ? clipboard : JsonUtil.extractJsonStr(clipboard);
            }

            if (StrUtil.isBlank(jsonStr) && LoadLastRecordAction.isLoadLastRecord()) {
                JsonViewerHistoryState state = JsonViewerHistoryState.getInstance(project);
                LimitedList<String> history = state.getHistory();
                if (CollUtil.isNotEmpty(history)) {
                    jsonStr = history.get(0);
                }
            }

            if (StrUtil.isNotBlank(jsonStr)) {
                jsonTextField.setText(jsonStr);
            }
        }
    }


    private void pasteJsonToEditor() {
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

    private void toggleLineNumbers() {
        EditorImpl editor = (EditorImpl) Objects.requireNonNull(jsonTextField.getEditor());
        DisplayLineNumberAction.showLineNumber(editor, DisplayLineNumberAction.isShownLineNumbers());
    }

    private void addJsonToHistory() {
        LimitedList<String> historyList = historyState.getHistory();
        String text = StrUtil.trim(jsonTextField.getText());

        if (JsonUtil.isJsonStr(text)) {
            historyList.add(text);
        }
    }

    private void storeCaretOffset() {
        Editor editor = jsonTextField.getEditor();
        if (editor == null) return;
        if (StrUtil.isBlank(jsonTextField.getText())) return;
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        this.lastOffset = primaryCaret.getOffset();
    }

    private void moveToOffset() {
        Editor editor = jsonTextField.getEditor();
        if (editor == null) return;
        if (StrUtil.isBlank(jsonTextField.getText())) return;
        editor.getCaretModel().getPrimaryCaret().moveToOffset(this.lastOffset);
    }

    private void storeFoldRegion() {
        this.lastRegions.clear();
        Editor editor = jsonTextField.getEditor();
        if (editor == null) return;
        FoldRegion[] allRegions = editor.getFoldingModel().getAllFoldRegions();
        if (ArrayUtil.isEmpty(allRegions)) return;
        for (FoldRegion region : allRegions) {
            if (!region.isExpanded()) {
                // 记录折叠区域
                this.lastRegions.add(region);
            }
        }
    }

    private void recoverFoldRegion() {
        if (CollUtil.isEmpty(this.lastRegions)) return;
        try {
            for (FoldRegion region : this.lastRegions) {
                if (region.isExpanded())
                    region.setExpanded(false);
            }
        } catch (Exception ignored) {
        }
    }

    private class FocusListenerImpl extends FocusAdapter {

        @Override
        public void focusGained(FocusEvent event) {
            // --------------------------- 获取焦点时
            // 获取剪贴板的 JSON 并设置到编辑器内
            pasteJsonToEditor();
            // 行号显示
            toggleLineNumbers();
            // 切换回光标位置
            moveToOffset();
            recoverFoldRegion();

        }

        @Override
        public void focusLost(FocusEvent event) {
            // --------------------------- 失去焦点时
            // 添加当前编辑器的 JSON 至历史记录
            addJsonToHistory();
            // 焦点丢失时记录当前编辑器内的光标位置，待下次重新获取焦点时，切换到该光标位置
            storeCaretOffset();
            storeFoldRegion();

        }
    }

}
