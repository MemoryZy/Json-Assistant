package cn.memoryzy.json.ui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.toolwindow.*;
import cn.memoryzy.json.enums.BackgroundColorPolicy;
import cn.memoryzy.json.model.LimitedList;
import cn.memoryzy.json.model.deserialize.ArrayWrapper;
import cn.memoryzy.json.model.deserialize.ObjectWrapper;
import cn.memoryzy.json.model.strategy.ClipboardTextConverter;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionContext;
import cn.memoryzy.json.service.persistent.EditorOptionsPersistentState;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.ui.component.ToolWindowPanel;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.UIManager;
import com.intellij.execution.impl.ConsoleViewUtil;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.SpellCheckingEditorCustomizationProvider;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.ex.FocusChangeListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.tools.SimpleActionGroup;
import com.intellij.ui.ErrorStripeEditorCustomization;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class JsonAssistantToolWindowComponentProvider {

    private final Project project;
    private final FileType editorFileType;
    private final boolean initWindow;
    private final JsonHistoryPersistentState historyState;
    private final EditorOptionsPersistentState persistentState;
    private EditorEx editor;

    public JsonAssistantToolWindowComponentProvider(Project project, FileType editorFileType, boolean initWindow) {
        this.project = project;
        this.editorFileType = editorFileType;
        this.initWindow = initWindow;
        this.historyState = JsonHistoryPersistentState.getInstance(project);
        this.persistentState = EditorOptionsPersistentState.getInstance();
    }

    public JComponent createRootPanel() {
        TextEditor textEditor = createEditorComponent();
        this.editor = (EditorEx) textEditor.getEditor();

        ToolWindowPanel rootPanel = new ToolWindowPanel(new BorderLayout(), this.editor);
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
        TextEditor textEditor = UIManager.createDefaultTextEditor(project, editorFileType, initText);
        EditorEx editor = (EditorEx) textEditor.getEditor();

        EditorSettings settings = editor.getSettings();
        // 行号显示
        settings.setLineNumbersShown(persistentState.displayLineNumbers);
        // 设置显示的缩进导轨
        settings.setIndentGuidesShown(true);
        // 折叠块显示
        settings.setFoldingOutlineShown(persistentState.foldingOutline);
        // 折叠块、行号所展示的区域
        settings.setLineMarkerAreaShown(false);
        // 显示设置插入符行（光标选中行会变黄）
        settings.setCaretRowShown(StrUtil.isNotBlank(initText));

        ErrorStripeEditorCustomization.DISABLED.customize(editor);
        Objects.requireNonNull(SpellCheckingEditorCustomizationProvider.getInstance().getDisabledCustomization()).customize(editor);

        EditorGutterComponentEx gutterComponentEx = editor.getGutterComponentEx();
        // 设置绘画背景
        gutterComponentEx.setPaintBackground(false);

        toggleColorSchema(editor, editor.getColorsScheme(), persistentState.backgroundColorPolicy);

        editor.setBorder(JBUI.Borders.empty());

        editor.addFocusListener(new FocusListenerImpl());
        editor.getDocument().addDocumentListener(new DocumentListenerImpl(editor));

        JComponent component = textEditor.getComponent();
        component.setFont(UIManager.consolasFont(15));

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
            if (persistentState.recognizeOtherFormats) {
                String clipboard = PlatformUtil.getClipboard();
                if (StrUtil.isNotBlank(clipboard)) {
                    // 尝试不同格式数据策略
                    ClipboardTextConversionContext context = new ClipboardTextConversionContext();
                    jsonStr = ClipboardTextConverter.applyConversionStrategies(context, clipboard);

                    if (StrUtil.isNotBlank(jsonStr)) {
                        jsonStr = JsonUtil.formatJson(jsonStr);
                    }
                }
            }

            if (StrUtil.isBlank(jsonStr) && persistentState.importHistory) {
                JsonHistoryPersistentState state = JsonHistoryPersistentState.getInstance(project);
                LimitedList history = state.getHistory();
                if (CollUtil.isNotEmpty(history)) {
                    jsonStr = history.get(0);
                }
            }

            return StrUtil.isNotBlank(jsonStr) ? jsonStr : "";
        }

        return "";
    }


    private void pasteJsonToEditor() {
        // TODO 配置开关加上 是否识别 JSON5


        // TODO 工具窗口，JSON5切换 使用 editor.setFile(); 试试 （配置开关）
        // TODO 如果 editor.setFile() 不行，就关闭初始选项卡，再创建一个新的，文本原样移动过去

        if (initWindow && persistentState.recognizeOtherFormats) {
            String text = editor.getDocument().getText();
            if (StrUtil.isBlank(text)) {
                String clipboard = PlatformUtil.getClipboard();
                if (StrUtil.isNotBlank(clipboard)) {
                    // 尝试不同格式数据策略
                    ClipboardTextConversionContext context = new ClipboardTextConversionContext();
                    String jsonStr = ClipboardTextConverter.applyConversionStrategies(context, clipboard);

                    if (StrUtil.isNotBlank(jsonStr)) {
                        jsonStr = JsonUtil.formatJson(jsonStr);
                        String finalJsonStr = jsonStr;
                        WriteCommandAction.runWriteCommandAction(project, () -> editor.getDocument().setText(Objects.requireNonNull(finalJsonStr)));
                    }
                }
            }
        }
    }

    private void addJsonToHistory() {
        LimitedList historyList = historyState.getHistory();
        String text = StrUtil.trim(editor.getDocument().getText());

        ApplicationManager.getApplication().invokeLater(() -> {
            if (JsonUtil.isJson(text)) {
                // 无元素，不添加
                if (JsonUtil.isJsonArray(text)) {
                    ArrayWrapper jsonArray = JsonUtil.parseArray(text);
                    if (jsonArray.isEmpty()) return;
                } else if (JsonUtil.isJsonObject(text)) {
                    ObjectWrapper jsonObject = JsonUtil.parseObject(text);
                    if (jsonObject.isEmpty()) return;
                }

                historyList.add(text, true);
            } else if (Json5Util.isJson5(text)) {
                if (Json5Util.isJson5Array(text)) {
                    ArrayWrapper arrayWrapper = Json5Util.parseArray(text);
                    if (CollUtil.isEmpty(arrayWrapper)) return;
                } else if (Json5Util.isJson5Object(text)) {
                    ObjectWrapper objectWrapper = Json5Util.parseObject(text);
                    if (MapUtil.isEmpty(objectWrapper)) return;
                }

                historyList.add(text, false);
            }
        });
    }

    public static void toggleLineNumbers(EditorEx editor, boolean display) {
        EditorSettings settings = editor.getSettings();
        // 如果需要显示行号，而编辑器正好是展示状态
        boolean shownLineNumbersStatus = settings.isLineNumbersShown();

        if (display) {
            if (shownLineNumbersStatus) return;
        } else {
            if (!shownLineNumbersStatus) return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            settings.setLineNumbersShown(display);
            editor.reinitSettings();
        });
    }

    public static void toggleColorSchema(EditorEx editor, EditorColorsScheme defaultColorsScheme, BackgroundColorPolicy backgroundColorPolicy) {
        EditorColorsScheme scheme = defaultColorsScheme;
        switch (backgroundColorPolicy) {
            case DEFAULT: {
                // 改为新配色
                scheme = ConsoleViewUtil.updateConsoleColorScheme(defaultColorsScheme);
                if (UISettings.getInstance().getPresentationMode()) {
                    scheme.setEditorFontSize(UISettings.getInstance().getPresentationModeFontSize());
                }
                break;
            }
            case FOLLOW_MAIN_EDITOR: {
                // 跟随IDE配色
                break;
            }
        }

        editor.setColorsScheme(scheme);
    }

    public static void toggleFoldingOutline(EditorEx editor, boolean show) {
        EditorSettings settings = editor.getSettings();
        boolean foldingOutlineShown = settings.isFoldingOutlineShown();
        if (show) {
            if (foldingOutlineShown) return;
        } else {
            if (!foldingOutlineShown) return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            settings.setFoldingOutlineShown(show);
            editor.reinitSettings();
        });
    }

    private class FocusListenerImpl implements FocusChangeListener {

        @Override
        public void focusGained(@NotNull Editor e) {
            // --------------------------- 获取焦点时
            // 获取剪贴板的 JSON 并设置到编辑器内
            pasteJsonToEditor();
            // 行号显示
            toggleLineNumbers(editor, persistentState.displayLineNumbers);
            // 配色切换
            toggleColorSchema(editor, EditorColorsManager.getInstance().getGlobalScheme(), persistentState.backgroundColorPolicy);
            // 切换展示折叠区域
            toggleFoldingOutline(editor, persistentState.foldingOutline);
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
