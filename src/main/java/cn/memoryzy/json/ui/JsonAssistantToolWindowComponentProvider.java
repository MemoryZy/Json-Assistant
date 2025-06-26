package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.action.toolwindow.*;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.DataTypeConstant;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.ColorScheme;
import cn.memoryzy.json.enums.DataFormatType;
import cn.memoryzy.json.event.ColorSchemeChangedEvent;
import cn.memoryzy.json.event.FoldingOutlineToggleEvent;
import cn.memoryzy.json.event.LineNumbersToggleEvent;
import cn.memoryzy.json.model.strategy.ClipboardTextConverter;
import cn.memoryzy.json.model.strategy.clipboard.Json5ConversionStrategy;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionContext;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionStrategy;
import cn.memoryzy.json.model.structure.StructureSetting;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.service.persistent.state.HistoryLimitedList;
import cn.memoryzy.json.service.persistent.state.JsonEntry;
import cn.memoryzy.json.service.persistent.state.v2.EditorBehaviorState;
import cn.memoryzy.json.service.persistent.state.v2.EditorVisualState;
import cn.memoryzy.json.service.persistent.state.v2.HistoryState;
import cn.memoryzy.json.service.persistent.state.v2.JsonRecord;
import cn.memoryzy.json.service.persistent.v2.HistoryManager;
import cn.memoryzy.json.service.persistent.v2.ToolWindowSettings;
import cn.memoryzy.json.ui.color.EditorBackgroundScheme;
import cn.memoryzy.json.ui.dialog.ManuallySaveHistoryDialog;
import cn.memoryzy.json.ui.dialog.PreviewClipboardDataDialog;
import cn.memoryzy.json.ui.listener.EditorLineChangeMonitor;
import cn.memoryzy.json.ui.listener.MainWindowFocusMonitor;
import cn.memoryzy.json.ui.panel.CombineCardLayout;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
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
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.SpellCheckingEditorCustomizationProvider;
import com.intellij.openapi.editor.actions.AbstractToggleUseSoftWrapsAction;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.tools.SimpleActionGroup;
import com.intellij.ui.ErrorStripeEditorCustomization;
import com.intellij.ui.content.Content;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
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
    public static final Key<String> PLUGIN_EDITOR_FLAG = Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".PLUGIN_EDITOR_FLAG");

    /**
     * 消息总线（应用级）
     */
    public static final MessageBusConnection APPLICATION_CONNECTION = ApplicationManager.getApplication().getMessageBus().connect(ToolWindowSettings.getInstance());

    private final Project project;
    private final FileType fileType;
    private final boolean isInitialTab;

    private final JsonHistoryPersistentState historyState;
    private final EditorBehaviorState behaviorState;
    private final EditorVisualState visualState;
    private final HistoryState historyOptionState;
    private final HistoryManager historyManager;

    /**
     * 当前编辑器
     */
    private EditorEx currentEditor;

    /**
     * 当前内容页
     */
    private Content currentContent;

    // TODO 这个必须改，不然线程池太多
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private final AtomicReference<ScheduledFuture<?>> pendingTask = new AtomicReference<>();

    public JsonAssistantToolWindowComponentProvider(Project project, FileType fileType, boolean isInitialTab) {
        this.project = project;
        this.fileType = fileType;
        this.isInitialTab = isInitialTab;
        // TODO 待修改
        this.historyState = JsonHistoryPersistentState.getInstance(project);

        ToolWindowSettings toolWindowSettings = ToolWindowSettings.getInstance();
        this.behaviorState = toolWindowSettings.getBehaviorState();
        this.visualState = toolWindowSettings.getVisualState();
        this.historyOptionState = toolWindowSettings.getHistoryState();
        this.historyManager = HistoryManager.getInstance(project);
    }

    public JComponent createComponent() {
        // 创建编辑器
        this.currentEditor = (EditorEx) PlatformUtil.createEditor(project, PluginConstant.MAIN_WINDOW_DISPLAY_NAME, fileType, false, EditorKind.MAIN_EDITOR, "");

        // 配置编辑器的外观
        configureEditorAppearance();
        // 配置编辑器的行为
        configureEditorBehavior();
        // 注册配置更新事件的处理器
        registerConfigurationUpdateEventHandlers();

        // 主面板（携带工具栏）
        SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(false, false);
        // 卡片布局
        CombineCardLayout cardLayout = new CombineCardLayout();
        // 卡片面板
        JPanel cardPanel = new JPanel(cardLayout);

        toolWindowPanel.setContent(createRootPanel(toolWindowPanel, cardLayout, cardPanel));
        toolWindowPanel.setToolbar(createToolbar(toolWindowPanel));
        toolWindowPanel.setProvideQuickActions(true);
        return toolWindowPanel;
    }

    private JsonAssistantToolWindowPanel createRootPanel(SimpleToolWindowPanel simpleToolWindowPanel, CombineCardLayout cardLayout, JPanel cardPanel) {
        JsonStructureComponentProvider treeProvider = new JsonStructureComponentProvider(null, simpleToolWindowPanel, getStructureSetting());
        JsonQueryComponentProvider queryProvider = new JsonQueryComponentProvider(project);
        JsonGridComponentProvider gridProvider = new JsonGridComponentProvider(null);
        Disposer.register(this, queryProvider);

        JsonAssistantToolWindowPanel rootPanel = new JsonAssistantToolWindowPanel(new BorderLayout())
                .setEditor(this.currentEditor)
                .setTreeProvider(treeProvider)
                .setQueryProvider(queryProvider)
                .setGridProvider(gridProvider)
                .setCardLayout(cardLayout);

        // Json 编辑器
        JComponent editorComponent = currentEditor.getComponent();
        // Json 树
        JPanel treeComponent = treeProvider.getTreeComponent();
        // Json 查询界面
        JComponent queryComponent = queryProvider.createComponent();
        // 表格组件
        JPanel tableComponent = gridProvider.getTableComponent();

        // 在工具窗口中，可能字体需略微调大一点
        resizeTreeFont(treeProvider);

        // 添加 Json 编辑器
        cardPanel.add(editorComponent, UIUtils.JSON_EDITOR_CARD_NAME);
        // 添加 Json 树
        cardPanel.add(treeComponent, UIUtils.JSON_TREE_CARD_NAME);
        // 添加 Json 查询界面
        cardPanel.add(queryComponent, UIUtils.JSON_QUERY_CARD_NAME);
        // 添加 表格 界面
        cardPanel.add(tableComponent, UIUtils.JSON_GRID_CARD_NAME);
        // 默认显示编辑器
        cardLayout.show(cardPanel, UIUtils.JSON_EDITOR_CARD_NAME);
        // 添加到面板
        rootPanel.add(cardPanel, BorderLayout.CENTER);

        return rootPanel;
    }

    private void resizeTreeFont(JsonStructureComponentProvider treeProvider) {
        Tree tree = treeProvider.getTree();
        Font font = tree.getFont();
        tree.setFont(font.deriveFont((float) (font.getSize() + 1)));
    }

    private void configureEditorAppearance() {
        EditorSettings settings = currentEditor.getSettings();
        // 行号显示
        settings.setLineNumbersShown(visualState.isShowLineNumbers());
        // 设置显示的缩进导轨
        settings.setIndentGuidesShown(true);
        // 折叠块显示
        settings.setFoldingOutlineShown(visualState.isShowFoldingOutline());
        // 折叠块、行号所展示的区域
        settings.setLineMarkerAreaShown(false);
        // 显示设置插入符行（光标选中行会变黄）
        settings.setCaretRowShown(true);

        ErrorStripeEditorCustomization.DISABLED.customize(currentEditor);
        Objects.requireNonNull(SpellCheckingEditorCustomizationProvider.getInstance().getDisabledCustomization()).customize(currentEditor);

        // 设置绘画背景
        EditorGutterComponentEx gutterComponentEx = currentEditor.getGutterComponentEx();
        gutterComponentEx.setPaintBackground(false);

        currentEditor.setBorder(JBUI.Borders.empty());

        // 指定配色方案
        applyColorScheme(currentEditor.getColorsScheme(), visualState.getColorScheme());

        JComponent component = currentEditor.getComponent();
        component.setFont(UIUtils.consolasFont(15));
        component.setBorder(JBUI.Borders.customLine(currentEditor.getBackgroundColor(), 0, 4, 0, 0));

        // 切换软换行状态
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String value = propertiesComponent.getValue(PluginConstant.SOFT_WRAPS_SELECT_STATE);
        if (null != value) {
            AbstractToggleUseSoftWrapsAction.toggleSoftWraps(currentEditor, null, Boolean.parseBoolean(value));
        }
    }

    private void configureEditorBehavior() {
        MainWindowFocusMonitor focusMonitor = new MainWindowFocusMonitor(behaviorState, historyOptionState, historyManager);
        Disposer.register(this, focusMonitor);

        currentEditor.addFocusListener(focusMonitor);
        currentEditor.getDocument().addDocumentListener(new EditorLineChangeMonitor(currentEditor));

        DumbAwareAction.create(event -> {
            if (historyOptionState.isEnableHistory() && !historyOptionState.isAutoRecordHistory()) {
                // 手动储存历史记录
                saveHistoryManually();
            }
        }).registerCustomShortcutSet(CustomShortcutSet.fromString("ctrl S"), currentEditor.getComponent());

        // 添加标记
        currentEditor.putUserData(PLUGIN_EDITOR_FLAG, JsonAssistantPlugin.PLUGIN_AUTHOR);
    }

    /**
     * 注册配置更新事件的处理器
     */
    private void registerConfigurationUpdateEventHandlers() {
        // 切换行号展示
        APPLICATION_CONNECTION.subscribe(LineNumbersToggleEvent.TOPIC, (LineNumbersToggleEvent) this::toggleLineNumbersVisibility);
        APPLICATION_CONNECTION.subscribe(FoldingOutlineToggleEvent.TOPIC, (FoldingOutlineToggleEvent) this::toggleFoldingOutlineVisibility);
        APPLICATION_CONNECTION.subscribe(ColorSchemeChangedEvent.TOPIC, (ColorSchemeChangedEvent) this::applyColorScheme);
    }

    public JComponent createToolbar(SimpleToolWindowPanel toolWindowPanel) {
        SimpleActionGroup actionGroup = new SimpleActionGroup();
        actionGroup.add(new JsonBeautifyToolWindowAction(currentEditor, toolWindowPanel));
        actionGroup.add(new JsonMinifyToolWindowAction(currentEditor, toolWindowPanel));
        actionGroup.add(Separator.create());
        actionGroup.add(new JsonStructureToolWindowAction(currentEditor, toolWindowPanel));
        actionGroup.add(new JsonQueryAction(currentEditor, toolWindowPanel));
        actionGroup.add(new JsonGridToolWindowAction(currentEditor, toolWindowPanel));
        actionGroup.add(Separator.create());
        actionGroup.add(new ToggleUseSoftWrapsAction(currentEditor, toolWindowPanel));
        actionGroup.add(new ScrollToTheEndAction(currentEditor, toolWindowPanel));
        actionGroup.add(Separator.create());
        actionGroup.add(new SaveToDiskAction(currentEditor, toolWindowPanel));
        actionGroup.add(new ClearEditorAction(currentEditor, toolWindowPanel));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actionGroup, false);
        toolbar.setTargetComponent(toolWindowPanel);
        return toolbar.getComponent();
    }

    private void toggleLineNumbersVisibility(boolean display) {
        EditorSettings settings = currentEditor.getSettings();
        // 如果需要显示行号，而编辑器正好是展示状态
        boolean shownLineNumbersStatus = settings.isLineNumbersShown();

        if (display) {
            if (shownLineNumbersStatus) return;
        } else {
            if (!shownLineNumbersStatus) return;
        }

        settings.setLineNumbersShown(display);
        currentEditor.reinitSettings();
    }

    private void toggleFoldingOutlineVisibility(boolean display) {
        EditorSettings settings = currentEditor.getSettings();
        boolean foldingOutlineShown = settings.isFoldingOutlineShown();

        if (display) {
            if (foldingOutlineShown) return;
        } else {
            if (!foldingOutlineShown) return;
        }

        settings.setFoldingOutlineShown(display);
        currentEditor.reinitSettings();
    }

    private void applyColorScheme(ColorScheme colorScheme) {
        applyColorScheme(EditorColorsManager.getInstance().getGlobalScheme(), colorScheme);
    }

    private void applyColorScheme(EditorColorsScheme defaultColorsScheme, ColorScheme colorScheme) {
        if (ColorScheme.Default.equals(colorScheme)) {
            // 默认的话，按照默认颜色
            Color oriColor = currentEditor.getBackgroundColor();
            Color newColor = defaultColorsScheme.getDefaultBackground();

            if (!Objects.equals(oriColor, newColor)) {
                // 需设置一遍将颜色变更回来
                currentEditor.setColorsScheme(defaultColorsScheme);
            }
        } else {
            // 其他的按照自身设定的颜色来操作
            // 判断是否已经是指定的颜色，防止每次都设置
            Color newColor = colorScheme.getColor();
            Color oriColor = currentEditor.getBackgroundColor();
            // 新颜色不为空，且不等于原先的旧颜色
            if (Objects.nonNull(newColor) && !Objects.equals(oriColor, newColor)) {
                currentEditor.setColorsScheme(new EditorBackgroundScheme(defaultColorsScheme, newColor));
            }
        }

        currentEditor.getComponent().setBorder(JBUI.Borders.customLine(currentEditor.getBackgroundColor(), 0, 4, 0, 0));
    }


    private void saveHistoryManually() {
        // TODO 手动保存的话，可以通过toolwindow.notifyBallon的方式提醒是否要加名字

        // 获取编辑器内容
        String content = StrUtil.trim(currentEditor.getDocument().getText());
        // 检查内容有效性
        if (StrUtil.isBlank(content)) return;

        // 解析格式
        JsonWrapper wrapper = null;
        DataFormatType formatType = DataFormatType.JSON;
        if (JsonUtil.isJson(content)) {
            wrapper = JsonUtil.parse(content);

        } else if (Json5Util.isJson5(content)) {
            formatType = DataFormatType.JSON5;
            wrapper = Json5Util.parse(content);
            // 由这里再进行格式化（不可避免会去掉一些Array上的注释）
            content = Json5Util.formatJson5WithComment(content);
        }

        if (null == wrapper || wrapper.noItems()) return;

        // 手动保存
        JsonRecord record = historyManager.find(wrapper);
        // 判断是否为新增
        boolean isAdd = null == record;

        if (isAdd) {
            // 新增
            historyManager.addEntry(record = new JsonRecord().setRawText(content).setSourceType(formatType).setWrapper(wrapper));
        }




        // 若已存在此记录，则更新其updateTime，以及后续指定名称



        // TODO 最简单的是只新增，不修改历史记录内容，碰到相同名的、相同结构的直接提醒

        // TODO 把历史记录做成一个toolwindow，这样看着更直观（看看是底部还是侧边）

    }


    private void pasteJsonToEditor() {
        if (isInitialTab && behaviorState.isAutoRecognizeFormats()) {
            String text = currentEditor.getDocument().getText();
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
                        if (behaviorState.isShouldPromptBeforeImport()) {
                            new PreviewClipboardDataDialog(project, currentEditor, type, formattedStr, clipboard).show();
                        } else {
                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                boolean isJson5 = DataTypeConstant.JSON5.equals(type);
                                DocumentEx document = currentEditor.getDocument();
                                PsiFile psiFile = PlatformUtil.getPsiFile(project, document);

                                PlatformUtil.setDocumentText(document, isJson5 ? clipboard : formattedStr);
                                CodeStyleManager.getInstance(project).reformatText(psiFile, 0, document.getTextLength());
                            });

                            // 提示粘贴成功的消息
                            ToolWindowManager.getInstance(project).notifyByBalloon(
                                    PluginConstant.JSON_ASSISTANT_TOOLWINDOW_ID,
                                    MessageType.INFO,
                                    JsonAssistantBundle.messageOnSystem("hint.paste.json"));
                        }
                    }
                }
            }
        }
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
        HistoryLimitedList historyList = historyState.history;

        boolean isJson5 = false;
        String text = StrUtil.trim(currentEditor.getDocument().getText());
        JsonWrapper jsonWrapper = null;
        if (JsonUtil.isJson(text)) {
            jsonWrapper = JsonUtil.parse(text);

        } else if (Json5Util.isJson5(text)) {
            isJson5 = true;
            jsonWrapper = Json5Util.parse(text);
            // 由这里再进行格式化（不可避免会去掉一些Array上的注释）
            text = Json5Util.formatJson5WithComment(text);
        }

        if (Objects.nonNull(jsonWrapper) && !jsonWrapper.noItems()) {
            // 判断之前是否存在此数据
            JsonEntry oldEntry = historyList.filterItem(jsonWrapper);
            String oldName = (null == oldEntry) ? "" : oldEntry.getName();

            // 保存（如果之前存在，则会将之前的删除，并顶到第一位，不存在则新建）
            JsonEntry newEntry = isJson5 ? historyList.add(project, jsonWrapper, text) : historyList.add(project, jsonWrapper);

            if (!auto) {
                NotificationAction skipAction = NotificationAction.createSimpleExpiring(JsonAssistantBundle.messageOnSystem("action.skip.text"), () -> {
                });
                NotificationAction assignNameAction = NotificationAction.createSimpleExpiring(JsonAssistantBundle.messageOnSystem("action.assign.name.text"), () -> {
                    // 当点击指定名称选项时，弹出窗口，要求填写名称
                    ManuallySaveHistoryDialog dialog = new ManuallySaveHistoryDialog(project, historyList, oldName);
                    if (dialog.showAndGet()) {
                        String newName = dialog.getNewName();
                        newEntry.setName(newName);

                        // 判断新名称是否与原来的名称一样，若是，则删掉原来的（排除新存储的记录）
                        JsonEntry jsonEntry = historyList.stream()
                                .filter(el -> Objects.equals(el.getName(), newName) && !Objects.equals(el.getId(), newEntry.getId()))
                                .findFirst()
                                .orElse(null);

                        if (Objects.nonNull(jsonEntry)) {
                            historyList.removeById(jsonEntry.getId());
                        }

                        // 1.数据相同，原名存在，更改后
                        // 2.数据不同，名字存在，覆盖后
                        // 3.新增，赋名
                        String tipContent;
                        if (Objects.isNull(oldEntry)) {
                            // 新增记录并赋予名称
                            tipContent = JsonAssistantBundle.messageOnSystem("hint.new.recordName");

                        } else if (!StrUtil.equals(oldName, newName)) {
                            // 数据相同，新旧名称不同
                            tipContent = JsonAssistantBundle.messageOnSystem("hint.update.recordName");

                        } else {
                            // 数据不同，名字相同，把旧数据覆盖
                            tipContent = JsonAssistantBundle.messageOnSystem("hint.update.recordData");
                        }

                        // 提示
                        ToolWindowManager.getInstance(project).notifyByBalloon(PluginConstant.JSON_ASSISTANT_TOOLWINDOW_ID, MessageType.INFO, tipContent);
                    }
                });

                // 通知
                Notifications.showNotification(
                        null,
                        JsonAssistantBundle.messageOnSystem("notification.save.history.content"),
                        NotificationType.INFORMATION,
                        Lists.newArrayList(assignNameAction, skipAction),
                        project);
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
        EditorFactory.getInstance().releaseEditor(currentEditor);
        // 清理资源
        cancelPendingTask();
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                LOG.error("[Json Assistant] The Executor does not shut down properly");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    public Content getCurrentContent() {
        return currentContent;
    }

    public void setCurrentContent(Content currentContent) {
        this.currentContent = currentContent;
    }

    private StructureSetting getStructureSetting() {
        return new StructureSetting().setNeedBorder(false).setNeedToolbar(true).setExpandLevel(3);
    }

}
