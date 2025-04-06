package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.toolwindow.*;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.DataTypeConstant;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.ColorScheme;
import cn.memoryzy.json.model.EditorInitData;
import cn.memoryzy.json.model.HistoryLimitedList;
import cn.memoryzy.json.model.JsonEntry;
import cn.memoryzy.json.model.StructureConfig;
import cn.memoryzy.json.model.strategy.ClipboardTextConverter;
import cn.memoryzy.json.model.strategy.clipboard.Json5ConversionStrategy;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionContext;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionStrategy;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.service.persistent.state.EditorAppearanceState;
import cn.memoryzy.json.service.persistent.state.EditorBehaviorState;
import cn.memoryzy.json.service.persistent.state.HistoryState;
import cn.memoryzy.json.ui.color.EditorBackgroundScheme;
import cn.memoryzy.json.ui.dialog.JsonHistoryTreeChooser;
import cn.memoryzy.json.ui.dialog.PreviewClipboardDataDialog;
import cn.memoryzy.json.ui.panel.CombineCardLayout;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
import cn.memoryzy.json.util.UIManager;
import cn.memoryzy.json.util.*;
import com.google.common.collect.Lists;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actions.AbstractToggleUseSoftWrapsAction;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.ex.FocusChangeListener;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.tools.SimpleActionGroup;
import com.intellij.ui.ErrorStripeEditorCustomization;
import com.intellij.ui.content.Content;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Consumer;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class JsonAssistantToolWindowComponentProvider implements Disposable {
    private static final Logger LOG = Logger.getInstance(JsonAssistantToolWindowComponentProvider.class);
    public static final String MANUAL_HISTORY_GUIDE_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".MANUAL_HISTORY_GUIDE_KEY";

    private final Project project;
    private final FileType editorFileType;
    private final boolean initTab;
    private final JsonHistoryPersistentState historyState;
    private final EditorBehaviorState editorBehaviorState;
    private final EditorAppearanceState editorAppearanceState;
    private final HistoryState historyOptionState;
    private final PropertiesComponent propertiesComponent;
    private EditorEx editor;

    private Content content;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private final AtomicReference<ScheduledFuture<?>> pendingTask = new AtomicReference<>();

    public JsonAssistantToolWindowComponentProvider(Project project, FileType editorFileType, boolean initTab) {
        this.project = project;
        this.editorFileType = editorFileType;
        this.initTab = initTab;
        this.historyState = JsonHistoryPersistentState.getInstance(project);
        JsonAssistantPersistentState persistentState = JsonAssistantPersistentState.getInstance();
        this.editorBehaviorState = persistentState.editorBehaviorState;
        this.editorAppearanceState = persistentState.editorAppearanceState;
        this.historyOptionState = persistentState.historyState;
        this.propertiesComponent = PropertiesComponent.getInstance();
    }

    public JComponent createComponent() {
        SimpleToolWindowPanel simpleToolWindowPanel = new SimpleToolWindowPanel(false, false);

        // 创建编辑器
        this.editor = createEditor();

        // 卡片布局
        CombineCardLayout cardLayout = new CombineCardLayout();
        // 卡片面板
        JPanel cardPanel = new JPanel(cardLayout);

        JsonStructureComponentProvider treeProvider = new JsonStructureComponentProvider(null, simpleToolWindowPanel, StructureConfig.of(false));
        JsonQueryComponentProvider queryProvider = new JsonQueryComponentProvider(project);
        Disposer.register(this, queryProvider);

        JsonAssistantToolWindowPanel rootPanel = createToolWindowPanel(treeProvider, queryProvider, cardLayout);

        // Json 编辑器
        JComponent editorComponent = editor.getComponent();
        // Json 树
        JPanel treeComponent = treeProvider.getTreeComponent();
        // Json 查询界面
        JComponent queryComponent = queryProvider.createComponent();

        // 在工具窗口中，可能字体需略微调大一点
        resizeTreeFont(treeProvider);

        // 添加 Json 编辑器
        cardPanel.add(editorComponent, PluginConstant.JSON_EDITOR_CARD_NAME);
        // 添加 Json 树
        cardPanel.add(treeComponent, PluginConstant.JSON_TREE_CARD_NAME);
        // 添加 JsonPath 界面
        cardPanel.add(queryComponent, PluginConstant.JSON_QUERY_CARD_NAME);
        // 默认显示编辑器
        cardLayout.show(cardPanel, PluginConstant.JSON_EDITOR_CARD_NAME);
        // 添加到面板
        rootPanel.add(cardPanel, BorderLayout.CENTER);

        simpleToolWindowPanel.setContent(rootPanel);
        simpleToolWindowPanel.setToolbar(createToolbar(simpleToolWindowPanel));
        // simpleToolWindowPanel.setProvideQuickActions(true);
        return simpleToolWindowPanel;
    }

    private void resizeTreeFont(JsonStructureComponentProvider treeProvider) {
        Tree tree = treeProvider.getTree();
        Font font = tree.getFont();
        tree.setFont(font.deriveFont((float) (font.getSize() + 1)));
    }

    private JsonAssistantToolWindowPanel createToolWindowPanel(JsonStructureComponentProvider treeProvider, JsonQueryComponentProvider queryProvider, CombineCardLayout cardLayout) {
        JsonAssistantToolWindowPanel rootPanel = new JsonAssistantToolWindowPanel(new BorderLayout());
        rootPanel.setEditor(this.editor);
        rootPanel.setTreeProvider(treeProvider);
        rootPanel.setQueryProvider(queryProvider);
        rootPanel.setCardLayout(cardLayout);
        return rootPanel;
    }


    private EditorEx createEditor() {
        EditorInitData initData = getInitData();
        boolean hasText = initData.isHasText();
        String jsonString = initData.getJsonString();
        String parseType = initData.getParseType();
        String originalText = initData.getOriginalText();
        boolean needPrompt = hasText && editorBehaviorState.promptBeforeImport;

        // 若是json5，则粘贴原文
        boolean isJson5 = DataTypeConstant.JSON5.equals(parseType);
        String editorText = isJson5 ? originalText : jsonString;

        EditorEx editor = (EditorEx) PlatformUtil.createEditor(
                project,
                "View." + editorFileType.getDefaultExtension(),
                editorFileType,
                false,
                EditorKind.MAIN_EDITOR,
                needPrompt ? "" : editorText);

        // 补充编辑器的外观
        changeEditorAppearance(editor, hasText);

        // 是否需要提示询问
        if (needPrompt) {
            new PreviewClipboardDataDialog(project, editor, parseType, jsonString, originalText).show();
        }

        if (hasText && !needPrompt) {
            // 格式化文本
            if (isJson5) {
                DocumentEx document = editor.getDocument();
                PsiFile psiFile = PlatformUtil.getPsiFile(project, document);
                WriteCommandAction.runWriteCommandAction(project,
                        () -> CodeStyleManager.getInstance(project).reformatText(psiFile, 0, document.getTextLength()));
            }

            hintEditor(500, JsonAssistantBundle.messageOnSystem("hint.paste.json"));
        }

        // 第一次提示
        String value = propertiesComponent.getValue(MANUAL_HISTORY_GUIDE_KEY);
        if (StrUtil.isBlank(value)) {
            // 提示
            NotificationAction configureAction = NotificationAction.createSimpleExpiring(JsonAssistantBundle.messageOnSystem("action.configure.text"),
                    () -> ShowSettingsUtil.getInstance().showSettingsDialog(project, JsonAssistantBundle.message("setting.display.name")));
            NotificationAction notAskAction = NotificationAction.createSimpleExpiring(JsonAssistantBundle.messageOnSystem("action.not.ask.text"), () -> {
            });
            ArrayList<NotificationAction> notificationActions = Lists.newArrayList(configureAction, notAskAction);
            Notifications.showFullStickyNotification("Json Assistant", JsonAssistantBundle.messageOnSystem("notification.manual.history.content"), NotificationType.INFORMATION, notificationActions, project);
            propertiesComponent.setValue(MANUAL_HISTORY_GUIDE_KEY, "1");
        }

        return editor;
    }

    private void changeEditorAppearance(EditorEx editor, boolean hasText) {
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
        settings.setCaretRowShown(hasText);

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

        // 手动储存历史记录
        DumbAwareAction.create(new ManuallySaveHistoryAction())
                .registerCustomShortcutSet(CustomShortcutSet.fromString("ctrl S"), editor.getComponent());

        JComponent component = editor.getComponent();
        component.setFont(UIManager.consolasFont(15));
        component.setBorder(JBUI.Borders.customLine(editor.getBackgroundColor(), 0, 4, 0, 0));

        // 切换软换行状态
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String value = propertiesComponent.getValue(PluginConstant.SOFT_WRAPS_SELECT_STATE);
        if (null != value) {
            AbstractToggleUseSoftWrapsAction.toggleSoftWraps(editor, null, Boolean.parseBoolean(value));
        }
    }


    public JComponent createToolbar(SimpleToolWindowPanel simpleToolWindowPanel) {
        SimpleActionGroup actionGroup = new SimpleActionGroup();
        actionGroup.add(new JsonBeautifyToolWindowAction(editor, simpleToolWindowPanel));
        actionGroup.add(new JsonMinifyToolWindowAction(editor, simpleToolWindowPanel));
        actionGroup.add(Separator.create());
        actionGroup.add(new JsonStructureToolWindowAction(editor, simpleToolWindowPanel));
        actionGroup.add(new JsonQueryAction(editor, simpleToolWindowPanel));
        actionGroup.add(Separator.create());
        actionGroup.add(new ToggleUseSoftWrapsAction(editor, simpleToolWindowPanel));
        actionGroup.add(new ScrollToTheEndAction(editor, simpleToolWindowPanel));
        actionGroup.add(Separator.create());
        actionGroup.add(new SaveToDiskAction(editor, simpleToolWindowPanel));
        actionGroup.add(new ClearEditorAction(editor, simpleToolWindowPanel));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actionGroup, false);
        return toolbar.getComponent();
    }


    /**
     * 获取初始化文本
     *
     * @return 初始化文本详细信息
     */
    private EditorInitData getInitData() {
        String jsonString = "";
        String parseType = null;
        String originalText = null;

        if (initTab) {
            if (editorBehaviorState.recognizeOtherFormats) {
                String clipboard = PlatformUtil.getClipboard();
                if (StrUtil.isNotBlank(clipboard)) {
                    // 尝试不同格式数据策略
                    ClipboardTextConversionContext context = new ClipboardTextConversionContext();
                    String processedText = ClipboardTextConverter.applyConversionStrategies(context, clipboard);

                    if (StrUtil.isNotBlank(processedText)) {
                        ClipboardTextConversionStrategy strategy = context.getStrategy();
                        parseType = strategy.type();
                        originalText = StrUtil.trim(clipboard);

                        JsonWrapper wrapper;
                        if (strategy instanceof Json5ConversionStrategy) {
                            wrapper = Json5Util.parse(processedText);
                            jsonString = Json5Util.formatJson5(processedText);
                        } else {
                            wrapper = JsonUtil.parse(processedText);
                            jsonString = JsonUtil.formatJson(processedText);
                        }

                        // 无属性或在拒绝黑名单里
                        if ((wrapper != null && wrapper.noItems()) || PreviewClipboardDataDialog.existsInBlacklist(wrapper)) {
                            jsonString = "";
                        }
                    }
                }
            }
        }

        return new EditorInitData(StrUtil.isNotBlank(jsonString), jsonString, parseType, originalText);
    }


    private void pasteJsonToEditor() {
        if (initTab && editorBehaviorState.recognizeOtherFormats) {
            String text = editor.getDocument().getText();
            if (StrUtil.isBlank(text)) {
                String clipboard = StrUtil.trim(PlatformUtil.getClipboard());
                if (StrUtil.isNotBlank(clipboard)) {
                    // 尝试不同格式数据策略
                    ClipboardTextConversionContext context = new ClipboardTextConversionContext();
                    String jsonStr = ClipboardTextConverter.applyConversionStrategies(context, clipboard);

                    if (StrUtil.isNotBlank(jsonStr)) {
                        ClipboardTextConversionStrategy strategy = context.getStrategy();
                        JsonWrapper wrapper;
                        String formattedStr;
                        if (strategy instanceof Json5ConversionStrategy) {
                            wrapper = Json5Util.parse(jsonStr);
                            formattedStr = Json5Util.formatJson5(jsonStr);
                        } else {
                            wrapper = JsonUtil.parse(jsonStr);
                            formattedStr = JsonUtil.formatJson(jsonStr);
                        }

                        // 过滤
                        if ((wrapper != null && wrapper.noItems()) || PreviewClipboardDataDialog.existsInBlacklist(wrapper)) {
                            return;
                        }

                        String type = strategy.type();
                        if (editorBehaviorState.promptBeforeImport) {
                            new PreviewClipboardDataDialog(project, editor, type, formattedStr, clipboard).show();
                        } else {
                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                boolean isJson5 = DataTypeConstant.JSON5.equals(type);
                                DocumentEx document = editor.getDocument();
                                PsiFile psiFile = PlatformUtil.getPsiFile(project, document);

                                PlatformUtil.setDocumentText(document, isJson5 ? clipboard : formattedStr);
                                CodeStyleManager.getInstance(project).reformatText(psiFile, 0, document.getTextLength());
                            });

                            // 提示粘贴成功的消息
                            hintEditor(400, JsonAssistantBundle.messageOnSystem("hint.paste.json"));
                        }
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

            HintUtil.showInformationHint(editor, msg);
        });
    }


    private void scheduleDebouncedAction() {
        // 取消之前的任务
        cancelPendingTask();

        // 提交新任务（500ms防抖窗口）
        ScheduledFuture<?> newTask = executor.schedule(() ->
                        SwingUtilities.invokeLater(() -> performAction(true)),
                3000, TimeUnit.MILLISECONDS
        );

        pendingTask.set(newTask);
    }


    private void performAction(boolean auto) {
        HistoryLimitedList historyList = historyState.getHistory();

        String text = StrUtil.trim(editor.getDocument().getText());
        JsonWrapper jsonWrapper = null;
        if (JsonUtil.isJson(text)) {
            jsonWrapper = JsonUtil.parse(text);

        } else if (Json5Util.isJson5(text)) {
            jsonWrapper = Json5Util.parse(text);
        }

        // TODO 保存时还需要保存原文，只限JSON5
        if (Objects.nonNull(jsonWrapper) && !jsonWrapper.noItems()) {
            if (auto) {
                historyList.add(project, jsonWrapper);
            } else {
                JsonEntry jsonEntry = historyList.filterItem(jsonWrapper);
                String oldName = (null == jsonEntry) ? "" : jsonEntry.getName();

                String newName = Messages.showInputDialog(
                        project,
                        null,
                        JsonAssistantBundle.messageOnSystem("dialog.assign.history.name.title"),
                        null,
                        oldName,
                        new JsonHistoryTreeChooser.NameValidator(historyList));

                if (StrUtil.isNotBlank(newName)) {
                    JsonEntry entry = historyList.add(project, jsonWrapper);
                    entry.setName(newName);
                    HintUtil.showInformationHint(editor, JsonAssistantBundle.messageOnSystem("hint.add.history"));
                }
            }
        }
    }

    private void cancelPendingTask() {
        ScheduledFuture<?> task = pendingTask.getAndSet(null);
        if (task != null && !task.isDone()) {
            task.cancel(false);
        }
    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(editor);
        // 清理资源
        cancelPendingTask();
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                LOG.error("The Executor does not shut down properly");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
            UIManager.repaintEditor(editor);
        });
    }

    public static void toggleColorSchema(EditorEx editor, EditorColorsScheme defaultColorsScheme, EditorAppearanceState appearanceState) {
        ColorScheme colorScheme = appearanceState.colorScheme;
        if (ColorScheme.Default.equals(colorScheme)) {
            // 默认的话，按照默认颜色
            Color oriColor = editor.getBackgroundColor();
            Color newColor = defaultColorsScheme.getDefaultBackground();

            if (!Objects.equals(oriColor, newColor)) {
                // 需设置一遍将颜色变更回来
                editor.setColorsScheme(defaultColorsScheme);
            }
        } else {
            // 其他的按照自身设定的颜色来操作
            // 判断是否已经是指定的颜色，防止每次都设置
            Color newColor = colorScheme.getColor();
            Color oriColor = editor.getBackgroundColor();
            // 新颜色不为空，且不等于原先的旧颜色
            if (Objects.nonNull(newColor) && !Objects.equals(oriColor, newColor)) {
                editor.setColorsScheme(new EditorBackgroundScheme(defaultColorsScheme, newColor));
            }
        }

        editor.getComponent().setBorder(JBUI.Borders.customLine(editor.getBackgroundColor(), 0, 4, 0, 0));

        UIManager.repaintEditor(editor);
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
            UIManager.repaintEditor(editor);
        });
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
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
            if (historyOptionState.switchHistory && historyOptionState.autoStore) {
                scheduleDebouncedAction();
            }
        }
    }

    private static class DocumentListenerImpl implements DocumentListener {
        private final EditorEx editor;
        private int lastLineCount = 0;

        public DocumentListenerImpl(EditorEx editor) {
            this.editor = editor;
        }

        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            try {
                // -------------- 开启/关闭光标行
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

                // -------------- 重新绘制
                DocumentEx document = editor.getDocument();
                int newLineCount = document.getLineCount();
                if (lastLineCount != newLineCount) {
                    lastLineCount = newLineCount;
                    UIManager.repaintEditor(editor);
                }

            } catch (Error error) {
                LOG.warn(error);
            }
        }
    }

    private class ManuallySaveHistoryAction implements Consumer<AnActionEvent> {
        @Override
        public void consume(AnActionEvent event) {
            if (historyOptionState.switchHistory && !historyOptionState.autoStore) {
                // TODO 需要手动提示（自定义弹出窗），存在同名的记录，则提示
                performAction(false);
            }
        }
    }

}
