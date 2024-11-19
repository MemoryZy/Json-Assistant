package cn.memoryzy.json.ui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.toolwindow.*;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.BackgroundColorScheme;
import cn.memoryzy.json.model.LimitedList;
import cn.memoryzy.json.model.strategy.ClipboardTextConverter;
import cn.memoryzy.json.model.strategy.clipboard.Json5ConversionStrategy;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionContext;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionStrategy;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.service.persistent.state.EditorAppearanceState;
import cn.memoryzy.json.service.persistent.state.EditorBehaviorState;
import cn.memoryzy.json.ui.color.EditorBackgroundScheme;
import cn.memoryzy.json.ui.component.ToolWindowPanel;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.UIManager;
import com.intellij.codeInsight.hint.HintManager;
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
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
    private final EditorBehaviorState editorBehaviorState;
    private final EditorAppearanceState editorAppearanceState;
    private EditorEx editor;

    public JsonAssistantToolWindowComponentProvider(Project project, FileType editorFileType, boolean initWindow) {
        this.project = project;
        this.editorFileType = editorFileType;
        this.initWindow = initWindow;
        this.historyState = JsonHistoryPersistentState.getInstance(project);
        JsonAssistantPersistentState persistentState = JsonAssistantPersistentState.getInstance();
        this.editorBehaviorState = persistentState.editorBehaviorState;
        this.editorAppearanceState = persistentState.editorAppearanceState;
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
        ImmutablePair<Boolean, String> pair = getInitText();
        boolean nonNull = Objects.nonNull(pair);
        String initText = nonNull ? pair.right : "";
        TextEditor textEditor = UIManager.createDefaultTextEditor(project, editorFileType, initText);
        EditorEx editor = (EditorEx) textEditor.getEditor();

        EditorSettings settings = editor.getSettings();
        // 行号显示
        settings.setLineNumbersShown(editorAppearanceState.displayLineNumbers);
        // 设置显示的缩进导轨
        settings.setIndentGuidesShown(true);
        // 折叠块显示
        settings.setFoldingOutlineShown(editorAppearanceState.foldingOutline);
        // 折叠块、行号所展示的区域
        settings.setLineMarkerAreaShown(false);
        // 显示设置插入符行（光标选中行会变黄）
        settings.setCaretRowShown(StrUtil.isNotBlank(initText));

        ErrorStripeEditorCustomization.DISABLED.customize(editor);
        Objects.requireNonNull(SpellCheckingEditorCustomizationProvider.getInstance().getDisabledCustomization()).customize(editor);

        EditorGutterComponentEx gutterComponentEx = editor.getGutterComponentEx();
        // 设置绘画背景
        gutterComponentEx.setPaintBackground(false);

        // 指定配色方案
        toggleColorSchema(editor, editor.getColorsScheme(), editorAppearanceState);

        editor.setBorder(JBUI.Borders.empty());

        editor.addFocusListener(new FocusListenerImpl());
        editor.getDocument().addDocumentListener(new DocumentListenerImpl(editor));

        JComponent component = textEditor.getComponent();
        component.setFont(UIManager.consolasFont(15));

        if (nonNull && StrUtil.isNotBlank(initText)) {
            hintEditor(500, pair.left
                    ? JsonAssistantBundle.messageOnSystem("hint.paste.json")
                    : JsonAssistantBundle.messageOnSystem("hint.import.json"));
        }

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


    /**
     * 获取初始化文本
     *
     * @return left: 是否为剪贴板数据；right: 初始化文本
     */
    private ImmutablePair<Boolean, String> getInitText() {
        if (initWindow) {
            boolean pasteData = false;
            String jsonStr = "";
            if (editorBehaviorState.recognizeOtherFormats) {
                String clipboard = PlatformUtil.getClipboard();
                if (StrUtil.isNotBlank(clipboard)) {
                    // 尝试不同格式数据策略
                    ClipboardTextConversionContext context = new ClipboardTextConversionContext();
                    jsonStr = ClipboardTextConverter.applyConversionStrategies(context, clipboard);

                    if (StrUtil.isNotBlank(jsonStr)) {
                        pasteData = true;
                        ClipboardTextConversionStrategy strategy = context.getStrategy();
                        jsonStr = (strategy instanceof Json5ConversionStrategy)
                                ? Json5Util.formatJson5(jsonStr)
                                : JsonUtil.formatJson(jsonStr);
                    }
                }
            }

            if (StrUtil.isBlank(jsonStr) && editorBehaviorState.importHistory) {
                JsonHistoryPersistentState state = JsonHistoryPersistentState.getInstance(project);
                LimitedList history = state.getHistory();
                if (CollUtil.isNotEmpty(history)) {
                    jsonStr = history.get(0);
                }
            }

            return ImmutablePair.of(pasteData, StrUtil.isNotBlank(jsonStr) ? jsonStr : "");
        }

        return null;
    }


    private void pasteJsonToEditor() {
        if (initWindow && editorBehaviorState.recognizeOtherFormats) {
            String text = editor.getDocument().getText();
            if (StrUtil.isBlank(text)) {
                String clipboard = PlatformUtil.getClipboard();
                if (StrUtil.isNotBlank(clipboard)) {
                    // 尝试不同格式数据策略
                    ClipboardTextConversionContext context = new ClipboardTextConversionContext();
                    String jsonStr = ClipboardTextConverter.applyConversionStrategies(context, clipboard);

                    if (StrUtil.isNotBlank(jsonStr)) {
                        ClipboardTextConversionStrategy strategy = context.getStrategy();
                        String formattedStr = (strategy instanceof Json5ConversionStrategy)
                                ? Json5Util.formatJson5(jsonStr)
                                : JsonUtil.formatJson(jsonStr);

                        WriteCommandAction.runWriteCommandAction(project, () -> PlatformUtil.setDocumentText(editor.getDocument(), formattedStr));

                        // 提示粘贴成功的消息
                        hintEditor(400, JsonAssistantBundle.messageOnSystem("hint.paste.json"));
                    }
                }
            }
        }
    }

    private void hintEditor(long millis, String msg) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ignored) {
            }

            HintManager.getInstance().showInformationHint(editor, msg);
        });
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

                String formattedJson = JsonUtil.formatJson(text);
                historyList.add(formattedJson, true);
            } else if (Json5Util.isJson5(text)) {
                if (Json5Util.isJson5Array(text)) {
                    ArrayWrapper arrayWrapper = Json5Util.parseArray(text);
                    if (CollUtil.isEmpty(arrayWrapper)) return;
                } else if (Json5Util.isJson5Object(text)) {
                    ObjectWrapper objectWrapper = Json5Util.parseObject(text);
                    if (MapUtil.isEmpty(objectWrapper)) return;
                }

                String formattedJson = Json5Util.formatJson5(text);
                historyList.add(formattedJson, false);
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

    public static void toggleColorSchema(EditorEx editor, EditorColorsScheme defaultColorsScheme, EditorAppearanceState appearanceState) {
        EditorColorsScheme scheme = defaultColorsScheme;
        BackgroundColorScheme colorScheme = appearanceState.backgroundColorScheme;
        switch (colorScheme) {
            case Default: {
                // 保持默认
                break;
            }
            case Classic:
            case Blue:
            case Green:
            case Orange:
            case Rose:
            case Violet:
            case Yellow:
            case Gray: {
                // 判断是否已经是指定的颜色，防止每次都设置
                Color color = colorScheme.getColor();
                Color backgroundColor = editor.getBackgroundColor();
                if (Objects.equals(backgroundColor, color)) {
                    return;
                }

                scheme = new EditorBackgroundScheme(scheme, color);
                break;
            }
            case Custom: {
                // 判断是否已经是指定的颜色，防止每次都设置
                Color backgroundColor = editor.getBackgroundColor();
                Color color = UIUtil.isUnderDarcula() ? appearanceState.customDarkcolor : appearanceState.customLightColor;
                if (Objects.equals(backgroundColor, color)) {
                    return;
                }

                // 如果设置了颜色，那就更换颜色
                if (null != color) {
                    scheme = new EditorBackgroundScheme(scheme, color);
                }

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
            toggleLineNumbers(editor, editorAppearanceState.displayLineNumbers);
            // 配色切换
            toggleColorSchema(editor, EditorColorsManager.getInstance().getGlobalScheme(), editorAppearanceState);
            // 切换展示折叠区域
            toggleFoldingOutline(editor, editorAppearanceState.foldingOutline);
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
            try {
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
            } catch (Error ignored) {
            }
        }
    }


}
