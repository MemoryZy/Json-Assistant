package cn.memoryzy.json.extension.editor;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.event.RefreshFloatToolbarEvent;
import cn.memoryzy.json.model.strategy.ClipboardTextConverter;
import cn.memoryzy.json.model.strategy.clipboard.Json5ConversionStrategy;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionContext;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.JsonAssistantToolWindowComponentProvider;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
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
    public static final int MAX_HASHES = 100;

    /**
     * 消息总线
     */
    private MessageBusConnection projectConnection;

    /**
     * 浮动工具栏组件
     */
    private static final Map<WeakReference<Editor>, FloatingToolbarComponent> FLOATING_COMPONENT_MAP = new WeakHashMap<>();

    /**
     * 存储所有已使用的剪贴板哈希值
     */
    private final Set<String> USED_HASHES = ConcurrentHashMap.newKeySet();

    @NotNull
    public ActionGroup getActionGroup() {
        return (ActionGroup) ActionHolder.EDITOR_FLOAT_GROUP;
    }

    public boolean getAutoHideable() {
        return false;
    }

    public int getPriority() {
        return 0;
    }

    public void register(@NotNull DataContext dataContext, @NotNull FloatingToolbarComponent component, @NotNull Disposable disposable) {
        // 如果用这个方式提供剪贴板数据，那么任何标签页都可以存在此功能
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null || editor.isDisposed()) {
            return;
        }

        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null || project.isDisposed()) {
            return;
        }

        // 是否为插件自定义的编辑器
        String userData = editor.getUserData(JsonAssistantToolWindowComponentProvider.PLUGIN_EDITOR_FLAG);
        if (StrUtil.isBlank(userData)) {
            return;
        }

        // 给自定义的编辑器添加事件订阅，编辑器得到焦点时，就触发一下这个事件
        if (null == projectConnection) {
            projectConnection = project.getMessageBus().connect(this);
            registerOnChangeHandlers();
        }

        // 缓存浮动工具栏
        FLOATING_COMPONENT_MAP.put(new WeakReference<>(editor), component);

        // 获取剪贴板数据
        String clipboard = PlatformUtil.getClipboard();
        if (StrUtil.isBlank(clipboard)) {
            return;
        }

        // 能否被转为 Json
        ClipboardTextConversionContext context = new ClipboardTextConversionContext();
        String processedText = ClipboardTextConverter.applyConversionStrategies(context, clipboard);
        if (StrUtil.isBlank(processedText)) {
            return;
        }

        // 是否含有值
        JsonWrapper wrapper = context.getStrategy() instanceof Json5ConversionStrategy
                ? Json5Util.parse(processedText)
                : JsonUtil.parse(processedText);

        if (null == wrapper || wrapper.noItems()) {
            return;
        }

        // 计算哈希值
        String hash = JsonAssistantUtil.calculateSHA256(clipboard);

        // 检查全局使用状态
        if (isHashUsedGlobally(hash)) {
            return;
        }

        // 展示
        component.scheduleShow();
    }

    /**
     * 兼容203版本
     */
    public void register(@NotNull FloatingToolbarComponent component, @NotNull Disposable disposable) {
        // TODO 将register方法实现抽取为公共方法，再进行实现（想办法获取DataContext对象）

        // DataManager manager = DataManager.getInstance();
// manager.getDataContextFromFocusAsync()

        // TODO 更稳妥的方式是用 FloatingToolbarComponent的实现类来获取，若获取不到，则取 实现类的 parentComponent、contextComponent 等


    }

    private void registerOnChangeHandlers() {
        // 更新工具栏组件状态
        projectConnection.subscribe(RefreshFloatToolbarEvent.ON_REFRESH_FLOAT_TOOLBAR, this::updateToolbarState);
        // 添加已使用的剪贴板数据

    }

    /**
     * 全局哈希检查
     */
    private boolean isHashUsedGlobally(String hash) {
        return USED_HASHES.contains(hash);
    }

    private void updateToolbarState(Editor editor) {
        // 获取剪贴板数据
        String clipboard = PlatformUtil.getClipboard();
        if (StrUtil.isBlank(clipboard)) {
            return;
        }

        // 能否被转为 Json
        ClipboardTextConversionContext context = new ClipboardTextConversionContext();
        String processedText = ClipboardTextConverter.applyConversionStrategies(context, clipboard);
        if (StrUtil.isBlank(processedText)) {
            return;
        }

        // 是否含有值
        JsonWrapper wrapper = context.getStrategy() instanceof Json5ConversionStrategy
                ? Json5Util.parse(processedText)
                : JsonUtil.parse(processedText);

        if (null == wrapper || wrapper.noItems()) {
            return;
        }

        // 计算哈希值
        String hash = JsonAssistantUtil.calculateSHA256(clipboard);

        // 检查全局使用状态
        if (isHashUsedGlobally(hash)) {
            return;
        }

        // 展示
        FloatingToolbarComponent component = null;
        for (WeakReference<Editor> ref : new ArrayList<>(FLOATING_COMPONENT_MAP.keySet())) {
            Editor cached = ref.get();
            if (cached == null || cached.isDisposed()) {
                // 清理无效引用
                FLOATING_COMPONENT_MAP.remove(ref);
            } else if (cached == editor) {
                component = FLOATING_COMPONENT_MAP.get(ref);
            }
        }

        if (null != component) {
            component.scheduleShow();
        }
    }

    /**
     * 注册全局使用
     */
    private void registerGlobalUsage(String hash) {
        // 自动清理旧记录，保持集合大小可控
        if (USED_HASHES.size() >= MAX_HASHES) {
            USED_HASHES.clear();
        }
        USED_HASHES.add(hash);
    }

    @Override
    public void dispose() {
        USED_HASHES.clear();
        projectConnection.disconnect();
    }

}
