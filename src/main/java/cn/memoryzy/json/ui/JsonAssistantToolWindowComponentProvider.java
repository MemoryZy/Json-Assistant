package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.action.toolwindow.*;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.ColorScheme;
import cn.memoryzy.json.enums.DataFormatType;
import cn.memoryzy.json.event.ColorSchemeChangedEvent;
import cn.memoryzy.json.event.FoldingOutlineToggleEvent;
import cn.memoryzy.json.event.LineNumbersToggleEvent;
import cn.memoryzy.json.model.structure.StructureSetting;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.state.v2.EditorBehaviorState;
import cn.memoryzy.json.service.persistent.state.v2.EditorVisualState;
import cn.memoryzy.json.service.persistent.state.v2.HistoryState;
import cn.memoryzy.json.service.persistent.state.v2.JsonRecord;
import cn.memoryzy.json.service.persistent.v2.HistoryManager;
import cn.memoryzy.json.service.persistent.v2.ToolWindowSettings;
import cn.memoryzy.json.ui.color.EditorBackgroundScheme;
import cn.memoryzy.json.ui.listener.EditorLineChangeMonitor;
import cn.memoryzy.json.ui.listener.MainWindowFocusMonitor;
import cn.memoryzy.json.ui.panel.CombineCardLayout;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
import cn.memoryzy.json.util.*;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.SpellCheckingEditorCustomizationProvider;
import com.intellij.openapi.editor.actions.AbstractToggleUseSoftWrapsAction;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
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
import com.intellij.tools.SimpleActionGroup;
import com.intellij.ui.ErrorStripeEditorCustomization;
import com.intellij.ui.content.Content;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class JsonAssistantToolWindowComponentProvider implements Disposable {

    private static final Logger LOG = Logger.getInstance(JsonAssistantToolWindowComponentProvider.class);
    public static final Key<String> PLUGIN_EDITOR_FLAG = Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".PLUGIN_EDITOR_FLAG");
    public static final String HISTORY_ADD_JUMP_KEY = "ADD";
    public static final String HISTORY_EXIST_JUMP_KEY = "EXIST";

    private final Project project;
    private final EditorBehaviorState behaviorState;
    private final EditorVisualState visualState;
    private final HistoryState historyState;
    private final HistoryManager historyManager;

    /**
     * 当前编辑器
     */
    private final EditorEx currentEditor;

    /**
     * 当前内容页
     */
    private Content currentContent;


    public JsonAssistantToolWindowComponentProvider(Project project, FileType fileType) {
        this.project = project;
        ToolWindowSettings toolWindowSettings = ToolWindowSettings.getInstance();
        this.behaviorState = toolWindowSettings.getBehaviorState();
        this.visualState = toolWindowSettings.getVisualState();
        this.historyState = toolWindowSettings.getHistoryState();
        this.historyManager = HistoryManager.getInstance(project);
        // 创建编辑器
        this.currentEditor = (EditorEx) PlatformUtil.createEditor(project, PluginConstant.MAIN_WINDOW_DISPLAY_NAME, fileType, false, EditorKind.MAIN_EDITOR, "");
    }

    public JComponent createComponent() {
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

        // 切换软换行状态
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String value = propertiesComponent.getValue(PluginConstant.SOFT_WRAPS_SELECT_STATE);
        if (null != value) {
            AbstractToggleUseSoftWrapsAction.toggleSoftWraps(currentEditor, null, Boolean.parseBoolean(value));
        }
    }

    private void configureEditorBehavior() {
        MainWindowFocusMonitor focusMonitor = new MainWindowFocusMonitor(behaviorState, historyState, historyManager);
        Disposer.register(this, focusMonitor);

        currentEditor.addFocusListener(focusMonitor);
        currentEditor.getDocument().addDocumentListener(new EditorLineChangeMonitor(currentEditor));

        DumbAwareAction.create(event -> {
            if (historyState.isEnableHistory() && !historyState.isAutoRecordHistory()) {
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
        ToolWindowUtil.APPLICATION_CONNECTION.subscribe(LineNumbersToggleEvent.TOPIC, (LineNumbersToggleEvent) this::toggleLineNumbersVisibility);
        ToolWindowUtil.APPLICATION_CONNECTION.subscribe(FoldingOutlineToggleEvent.TOPIC, (FoldingOutlineToggleEvent) this::toggleFoldingOutlineVisibility);
        ToolWindowUtil.APPLICATION_CONNECTION.subscribe(ColorSchemeChangedEvent.TOPIC, (ColorSchemeChangedEvent) this::applyColorScheme);
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
    }


    private void saveHistoryManually() {
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

        // 查找
        JsonRecord record = historyManager.find(wrapper);
        // 判断是否为新增
        boolean isAdd = (null == record);
        // 提示信息
        String message;

        if (isAdd) {
            // 新增
            historyManager.addEntry(new JsonRecord().setRawText(content).setSourceType(formatType).setWrapper(wrapper));
            message = JsonAssistantBundle.messageOnSystem("hint.manual.history.add.tip", HISTORY_ADD_JUMP_KEY);
        } else {
            message = JsonAssistantBundle.messageOnSystem("hint.manual.history.exist.tip", HISTORY_EXIST_JUMP_KEY);
        }

        // 提示粘贴成功的消息
        ToolWindowManager.getInstance(project).notifyByBalloon(
                PluginConstant.JSON_ASSISTANT_TOOLWINDOW_ID,
                MessageType.INFO,
                message,
                null,
                e -> {
                    String url = e.getDescription();
                    if (Objects.equals(HISTORY_ADD_JUMP_KEY, url)) {
                        // TODO 打开历史记录窗口，展示刚添加的记录，给名称编辑器指定焦点

                    } else if (Objects.equals(HISTORY_EXIST_JUMP_KEY, url)) {
                        // TODO 打开历史记录窗口，展示这条重复记录，给名称编辑器指定焦点

                    }
                });
    }


    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(currentEditor);
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
