package cn.memoryzy.json.ui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.toolwindow.*;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.LimitedList;
import cn.memoryzy.json.service.JsonViewerHistoryPersistentState;
import cn.memoryzy.json.ui.component.JsonViewerPanel;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.UIManager;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.ex.FocusChangeListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.tools.SimpleActionGroup;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class JsonViewerComponentProvider {

    private final Project project;
    private final boolean firstContent;
    private final boolean initWindow;
    private final JsonViewerHistoryPersistentState historyState;
    private EditorEx editor;
    private EditorColorsScheme defaultColorsScheme;

    public JsonViewerComponentProvider(Project project, boolean firstContent, boolean initWindow) {
        this.project = project;
        this.firstContent = firstContent;
        this.initWindow = initWindow;
        this.historyState = JsonViewerHistoryPersistentState.getInstance(project);
    }

    public JComponent createRootPanel() {
        TextEditor textEditor = createEditorComponent();
        this.editor = (EditorEx) textEditor.getEditor();

        JsonViewerPanel rootPanel = new JsonViewerPanel(new BorderLayout(), this.editor, this.defaultColorsScheme);
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(textEditor.getComponent(), BorderLayout.CENTER);
        rootPanel.add(centerPanel, BorderLayout.CENTER);

        SimpleToolWindowPanel simpleToolWindowPanel = new SimpleToolWindowPanel(false, false);
        simpleToolWindowPanel.setContent(rootPanel);
        simpleToolWindowPanel.setToolbar(createToolbar(simpleToolWindowPanel));
        return simpleToolWindowPanel;
    }

    private TextEditor createEditorComponent() {
        String initText = getInitText();
        TextEditor textEditor = UIManager.createDefaultTextEditor(project, JsonFileType.INSTANCE, initText);
        EditorEx editor = (EditorEx) textEditor.getEditor();

        EditorSettings settings = editor.getSettings();
        // 行号显示
        toggleLineNumbers(editor);
        // 设置显示的缩进导轨
        settings.setIndentGuidesShown(true);
        // 折叠块显示
        settings.setFoldingOutlineShown(true);
        // 折叠块、行号所展示的区域
        settings.setLineMarkerAreaShown(false);
        // 显示设置插入符行（光标选中行会变黄）
        settings.setCaretRowShown(StrUtil.isNotBlank(initText));

        EditorGutterComponentEx gutterComponentEx = editor.getGutterComponentEx();
        // 设置绘画背景
        gutterComponentEx.setPaintBackground(false);

        this.defaultColorsScheme = editor.getColorsScheme();
        FollowEditorThemeAction.changeColorSchema(editor, defaultColorsScheme, FollowEditorThemeAction.isFollowEditorTheme());

        if (firstContent) {
            editor.setPlaceholder(JsonAssistantBundle.messageOnSystem("placeholder.json.viewer.text"));
            editor.setShowPlaceholderWhenFocused(true);
        }

        editor.addFocusListener(new FocusListenerImpl());
        editor.getDocument().addDocumentListener(new DocumentListenerImpl(editor));

        JComponent component = textEditor.getComponent();
        component.setFont(new Font("Consolas", Font.PLAIN, 15));

        return textEditor;
    }

    public JComponent createToolbar(SimpleToolWindowPanel simpleToolWindowPanel) {
        SimpleActionGroup actionGroup = new SimpleActionGroup();
        actionGroup.add(new JsonBeautifyToolWindowAction(editor, simpleToolWindowPanel));
        actionGroup.add(new JsonMinifyToolWindowAction(editor, simpleToolWindowPanel));
        actionGroup.add(Separator.create());
        actionGroup.add(new JsonStructureToolWindowAction(editor, simpleToolWindowPanel));
        actionGroup.add(new JsonPathAction(editor, simpleToolWindowPanel));
        actionGroup.add(Separator.create());
        actionGroup.add(new SaveToDiskAction(editor));
        actionGroup.add(new ClearEditorAction(editor));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actionGroup, false);
        return toolbar.getComponent();
    }

    private String getInitText() {
        if (initWindow) {
            String jsonStr = "";
            String clipboard = PlatformUtil.getClipboard();
            if (StrUtil.isNotBlank(clipboard)) {
                jsonStr = (JsonUtil.isJsonStr(clipboard)) ? clipboard : JsonUtil.extractJsonStr(clipboard);
            }

            if (StrUtil.isBlank(jsonStr) && LoadLastRecordAction.isLoadLastRecord()) {
                JsonViewerHistoryPersistentState state = JsonViewerHistoryPersistentState.getInstance(project);
                LimitedList<String> history = state.getHistory();
                if (CollUtil.isNotEmpty(history)) {
                    jsonStr = history.get(0);
                }
            }

            return StrUtil.isNotBlank(jsonStr) ? jsonStr : "";
        }

        return "";
    }


    private void pasteJsonToEditor() {
        if (initWindow) {
            String text = editor.getDocument().getText();
            if (StrUtil.isBlank(text)) {
                String clipboard = PlatformUtil.getClipboard();
                if (StrUtil.isNotBlank(clipboard)) {
                    String jsonStr = (JsonUtil.isJsonStr(clipboard)) ? clipboard : JsonUtil.extractJsonStr(clipboard);
                    if (StrUtil.isNotBlank(jsonStr)) {
                        jsonStr = JsonUtil.formatJson(jsonStr);
                        String finalJsonStr = jsonStr;
                        WriteCommandAction.runWriteCommandAction(project, () -> editor.getDocument().setText(finalJsonStr));
                    }
                }
            }
        }
    }

    private void toggleLineNumbers(EditorEx editor) {
        DisplayLineNumberAction.showLineNumber(editor, DisplayLineNumberAction.isShownLineNumbers());
    }

    private void addJsonToHistory() {
        LimitedList<String> historyList = historyState.getHistory();
        String text = StrUtil.trim(editor.getDocument().getText());

        if (JsonUtil.isJsonStr(text)) {
            historyList.add(text);
        }
    }


    private class FocusListenerImpl implements FocusChangeListener {

        @Override
        public void focusGained(@NotNull Editor e) {
            // --------------------------- 获取焦点时
            // 获取剪贴板的 JSON 并设置到编辑器内
            pasteJsonToEditor();
            // 行号显示
            toggleLineNumbers(editor);
        }

        @Override
        public void focusLost(@NotNull Editor editor) {
            // --------------------------- 失去焦点时
            // 添加当前编辑器的 JSON 至历史记录
            addJsonToHistory();
        }
    }

    private static class DocumentListenerImpl implements DocumentListener {
        private final EditorEx editor;
        public DocumentListenerImpl(EditorEx editor) {
            this.editor = editor;
        }

        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            EditorSettings settings = editor.getSettings();
            // 编辑器原来为空，新增不为空，表示新增
            if (StrUtil.isBlank(event.getOldFragment()) && StrUtil.isNotBlank(event.getNewFragment())) {
                if (!settings.isCaretRowShown()) {
                    settings.setCaretRowShown(true);
                }
            } else if (StrUtil.isNotBlank(event.getOldFragment()) && StrUtil.isBlank(event.getNewFragment())) {
                // 编辑器原来不为空，新增为空，表示全部删除
                if (settings.isCaretRowShown()) {
                    settings.setCaretRowShown(false);
                }
            }
        }
    }

}
