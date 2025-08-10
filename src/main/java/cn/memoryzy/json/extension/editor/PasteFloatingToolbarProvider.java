package cn.memoryzy.json.extension.editor;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.event.RefreshFloatToolbarEvent;
import cn.memoryzy.json.event.RegisterClipboardUsageEvent;
import cn.memoryzy.json.model.strategy.ClipboardTextConverter;
import cn.memoryzy.json.model.strategy.clipboard.Json5ConversionStrategy;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionContext;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.ToolWindowSettings;
import cn.memoryzy.json.service.persistent.state.EditorBehaviorState;
import cn.memoryzy.json.ui.JsonAssistantToolWindowComponentProvider;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Memory
 * @since 2025/6/23
 */
@SuppressWarnings("DuplicatedCode")
public class PasteFloatingToolbarProvider implements FloatingToolbarProvider, Disposable {

    /**
     * 最大存储量
     */
    public static final int MAX_HASHES = 200;

    /**
     * 消息总线（应用级）
     */
    private MessageBusConnection applicationConnection;

    /**
     * 浮动工具栏组件
     */
    private final Map<Editor, FloatingToolbarComponent> floatingComponentMap = new WeakHashMap<>();

    /**
     * 存储所有已使用的剪贴板数据哈希值
     */
    private final Set<String> usedHashes = ConcurrentHashMap.newKeySet();

    private final EditorBehaviorState behaviorState;

    public PasteFloatingToolbarProvider() {
        this.behaviorState = ToolWindowSettings.getInstance().getBehaviorState();
    }

    @NotNull
    public ActionGroup getActionGroup() {
        return (ActionGroup) ActionHolder.EDITOR_FLOAT_GROUP;
    }

    public boolean getAutoHideable() {
        return false;
    }

    @SuppressWarnings("UnstableApiUsage")
    public int getPriority() {
        return 0;
    }

    public void register(@NotNull DataContext dataContext, @NotNull FloatingToolbarComponent component, @NotNull Disposable disposable) {
        // 如果用这个方式提供剪贴板数据，那么任何标签页都可以存在此功能
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null || editor.isDisposed()) {
            component.scheduleHide();
            return;
        }

        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null || project.isDisposed()) {
            component.scheduleHide();
            return;
        }

        // 是否为插件自定义的编辑器
        String userData = editor.getUserData(JsonAssistantToolWindowComponentProvider.PLUGIN_EDITOR_FLAG);
        if (StrUtil.isBlank(userData)) {
            component.scheduleHide();
            return;
        }

        if (null == applicationConnection) {
            // 只添加一次事件订阅
            applicationConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
            // 给自定义的编辑器添加事件订阅
            registerEventHandlers();
        }

        // 缓存浮动工具栏
        floatingComponentMap.put(editor, component);

        // 获取剪贴板数据
        String clipboard = StrUtil.trim(PlatformUtil.getClipboard());
        if (StrUtil.isBlank(clipboard)) {
            component.scheduleHide();
            return;
        }

        // 能否被转为 Json
        ClipboardTextConversionContext context = new ClipboardTextConversionContext();
        String processedText = ClipboardTextConverter.applyConversionStrategies(context, clipboard);
        if (StrUtil.isBlank(processedText)) {
            component.scheduleHide();
            return;
        }

        // 是否含有值
        JsonWrapper wrapper = context.getStrategy() instanceof Json5ConversionStrategy
                ? Json5Util.parse(processedText)
                : JsonUtil.parse(processedText);

        if (null == wrapper || wrapper.noItems()) {
            component.scheduleHide();
            return;
        }

        // 计算哈希值
        String hash = JsonAssistantUtil.calculateSHA256(clipboard);

        // 检查全局使用状态
        if (isHashUsedGlobally(hash)) {
            component.scheduleHide();
            return;
        }

        // 展示
        component.scheduleShow();
    }

    /**
     * 兼容203版本（在203版本 IDE 中，register方法执行的时机早于给编辑器赋予自定义标记的时机，所以先记录下可能是自定义编辑器的那些，随后再进行判断）
     */
    @SuppressWarnings("unchecked")
    public void register(@NotNull FloatingToolbarComponent component, @NotNull Disposable disposable) {
        DataManager manager = DataManager.getInstance();
        DataContext dataContext = manager.getDataContext((Component) component);

        // 如果用这个方式提供剪贴板数据，那么任何标签页都可以存在此功能
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null || editor.isDisposed()) {
            component.scheduleHide();
            return;
        }

        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project != null && project.isDisposed()) {
            component.scheduleHide();
            return;
        }

        Key<VirtualFile> fileKey = (Key<VirtualFile>) JsonAssistantUtil.readStaticFinalFieldValue(FileDocumentManagerBase.class, "FILE_KEY");
        if (null == fileKey) {
            component.scheduleHide();
            return;
        }

        Document document = editor.getDocument();
        VirtualFile virtualFile = document.getUserData(fileKey);
        if (null == virtualFile) {
            component.scheduleHide();
            return;
        }

        // 暂时将这种名字的编辑器当作是自定义编辑器
        String fileName = PluginConstant.MAIN_WINDOW_DISPLAY_NAME + ".json5";
        if (!Objects.equals(fileName, virtualFile.getName())) {
            component.scheduleHide();
            return;
        }

        if (null == applicationConnection) {
            // 只添加一次事件订阅
            applicationConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
            // 给自定义的编辑器添加事件订阅
            registerEventHandlers();
        }

        // 缓存浮动工具栏（因为不一定都是自定义编辑器，所以在此不显示 toolbar）
        floatingComponentMap.put(editor, component);
    }


    /**
     * 注册事件处理器（消费者）
     */
    private void registerEventHandlers() {
        // 更新工具栏组件状态
        applicationConnection.subscribe(RefreshFloatToolbarEvent.TOPIC, (RefreshFloatToolbarEvent) this::updateToolbarState);
        // 添加已使用的剪贴板数据
        applicationConnection.subscribe(RegisterClipboardUsageEvent.TOPIC, (RegisterClipboardUsageEvent) this::registerGlobalUsage);
    }

    /**
     * 全局哈希检查
     */
    public boolean isHashUsedGlobally(String hash) {
        return usedHashes.contains(hash);
    }


    /**
     * 更新工具栏组件状态
     */
    private void updateToolbarState(Editor editor, String clipboard) {
        FloatingToolbarComponent component = findAndCleanFloatingToolbar(editor);
        if (null == component) return;

        // 配置允许了，并且当前编辑器内容为空
        if (!behaviorState.isAutoRecognizeFormats() || StrUtil.isNotBlank(editor.getDocument().getText())) {
            component.scheduleHide();
            return;
        }

        // 判断剪贴板数据是否为空
        if (StrUtil.isBlank(clipboard)) {
            component.scheduleHide();
            return;
        }

        // 能否被转为 Json
        ClipboardTextConversionContext context = new ClipboardTextConversionContext();
        String processedText = ClipboardTextConverter.applyConversionStrategies(context, clipboard);
        if (StrUtil.isBlank(processedText)) {
            component.scheduleHide();
            return;
        }

        // 是否含有值
        JsonWrapper wrapper = context.getStrategy() instanceof Json5ConversionStrategy
                ? Json5Util.parse(processedText)
                : JsonUtil.parse(processedText);

        if (null == wrapper || wrapper.noItems()) {
            component.scheduleHide();
            return;
        }

        // 计算哈希值
        String hash = JsonAssistantUtil.calculateSHA256(clipboard);

        // 检查全局使用状态
        if (isHashUsedGlobally(hash)) {
            component.scheduleHide();
            return;
        }

        component.scheduleShow();
    }

    /**
     * 注册全局使用
     */
    private void registerGlobalUsage(Editor editor, String hash) {
        // 自动清理旧记录，保持集合大小可控
        if (usedHashes.size() >= MAX_HASHES) {
            usedHashes.clear();
        }
        usedHashes.add(hash);

        // 隐藏工具栏组件
        FloatingToolbarComponent component = findAndCleanFloatingToolbar(editor);
        if (null != component) component.scheduleHide();
    }

    private FloatingToolbarComponent findAndCleanFloatingToolbar(Editor editor) {
        FloatingToolbarComponent component = null;
        for (Editor cached : new ArrayList<>(floatingComponentMap.keySet())) {
            if (cached == null || cached.isDisposed()) {
                // 清理无效引用
                floatingComponentMap.remove(cached);
            } else if (cached == editor) {
                // 如果是旧版本的 FloatingToolbarProvider，那么还需要判断是否为自定义的编辑器
                if (JsonAssistantPlugin.LEGACY_FLOATING_TOOLBAR_PROVIDER) {
                    String userData = editor.getUserData(JsonAssistantToolWindowComponentProvider.PLUGIN_EDITOR_FLAG);
                    if (StrUtil.isBlank(userData)) {
                        // 清除非自定义的编辑器
                        floatingComponentMap.remove(cached);
                    } else {
                        component = floatingComponentMap.get(cached);
                    }

                } else {
                    component = floatingComponentMap.get(cached);
                }
            }
        }

        return component;
    }

    @Override
    public void dispose() {
        usedHashes.clear();
        floatingComponentMap.clear();
        applicationConnection.disconnect();
    }

}
