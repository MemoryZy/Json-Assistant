package cn.memoryzy.json.extension.editor;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.event.HistoryEditorFocusGainedEvent;
import cn.memoryzy.json.event.HistoryWindowEditEvent;
import cn.memoryzy.json.event.HistoryWindowExitEditEvent;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * @author Memory
 * @since 2025/7/7
 */
public class ImportHistoryFloatingToolbarProvider implements FloatingToolbarProvider, Disposable {

    /**
     * 浮动工具栏组件
     */
    private final Map<Editor, FloatingToolbarComponent> floatingComponentMap = new WeakHashMap<>();

    /**
     * 消息总线（应用级）
     */
    private MessageBusConnection applicationConnection;


    @SuppressWarnings("UnstableApiUsage")
    public int getPriority() {
        return 0;
    }

    public boolean getAutoHideable() {
        return false;
    }

    public @NotNull ActionGroup getActionGroup() {
        return (ActionGroup) ActionHolder.HISTORY_FLOAT_GROUP;
    }


    public void register(@NotNull DataContext dataContext, @NotNull FloatingToolbarComponent component, @NotNull Disposable disposable) {
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
        String userData = editor.getUserData(HistoryToolWindowComponentProvider.HISTORY_EDITOR_FLAG);
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

        // 缓存浮动工具栏（每个项目只会有一个编辑器）
        floatingComponentMap.put(editor, component);

        // 默认都予展示
        component.scheduleShow();
    }

    /**
     * 兼容203版本（在203版本 IDE 中，register方法执行的时机早于给编辑器赋予自定义标记的时机，所以先记录下可能是自定义编辑器的那些，随后再进行判断）
     */
    @SuppressWarnings("unchecked")
    public void register(@NotNull FloatingToolbarComponent component, @NotNull Disposable disposable) {
        DataManager manager = DataManager.getInstance();
        DataContext dataContext = manager.getDataContext((Component) component);
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

        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (null == file) {
            component.scheduleHide();
            return;
        }

        // 暂时将这种名字的编辑器当作是自定义编辑器
        String fileName = PluginConstant.HISTORY_EDITOR_NAME + ".json5";
        if (!Objects.equals(fileName, file.getName())) {
            component.scheduleHide();
            return;
        }

        if (null == applicationConnection) {
            // 只添加一次事件订阅
            applicationConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
            // 给自定义的编辑器添加事件订阅
            registerEventHandlers();
        }

        // 缓存浮动工具栏（每个项目只会有一个编辑器）
        floatingComponentMap.put(editor, component);
    }

    private void registerEventHandlers() {
        applicationConnection.subscribe(HistoryWindowEditEvent.TOPIC, (HistoryWindowEditEvent) this::hideToolbar);
        applicationConnection.subscribe(HistoryWindowExitEditEvent.TOPIC, (HistoryWindowExitEditEvent) this::showToolbar);
        if (JsonAssistantPlugin.LEGACY_FLOATING_TOOLBAR_PROVIDER) {
            applicationConnection.subscribe(HistoryEditorFocusGainedEvent.TOPIC, (HistoryEditorFocusGainedEvent) this::showToolbarOnFocusGained);
        }
    }

    private void hideToolbar(Editor editor) {
        FloatingToolbarComponent component = findAndCleanFloatingToolbar(editor);
        if (null != component) component.scheduleHide();
    }

    private void showToolbar(Editor editor) {
        FloatingToolbarComponent component = findAndCleanFloatingToolbar(editor);
        if (null != component) component.scheduleShow();
    }

    private void showToolbarOnFocusGained(Editor editor) {
        Pair<Editor, FloatingToolbarComponent> pair = findEditorAndCleanFloatingToolbar(editor);
        if (null != pair) {
            Editor currentEditor = pair.getFirst();
            FloatingToolbarComponent component = pair.getSecond();

            // 判断当前编辑器是否处于编辑模式，如果是，则不展示工具栏
            if (Boolean.TRUE.equals(currentEditor.getUserData(HistoryToolWindowComponentProvider.EDIT_MODE_FLAG))) {
                return;
            }

            component.scheduleShow();
        }
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
                    String userData = editor.getUserData(HistoryToolWindowComponentProvider.HISTORY_EDITOR_FLAG);
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

    private Pair<Editor, FloatingToolbarComponent> findEditorAndCleanFloatingToolbar(Editor editor) {
        Pair<Editor, FloatingToolbarComponent> pair = null;
        for (Editor cached : new ArrayList<>(floatingComponentMap.keySet())) {
            if (cached == null || cached.isDisposed()) {
                // 清理无效引用
                floatingComponentMap.remove(cached);

            } else if (cached == editor) {
                // 如果是旧版本的 FloatingToolbarProvider，那么还需要判断是否为自定义的编辑器
                if (JsonAssistantPlugin.LEGACY_FLOATING_TOOLBAR_PROVIDER) {
                    String userData = editor.getUserData(HistoryToolWindowComponentProvider.HISTORY_EDITOR_FLAG);
                    if (StrUtil.isBlank(userData)) {
                        // 清除非自定义的编辑器
                        floatingComponentMap.remove(cached);
                    } else {
                        pair = Pair.pair(cached, floatingComponentMap.get(cached));
                    }

                } else {
                    pair = Pair.pair(cached, floatingComponentMap.get(cached));
                }
            }
        }

        return pair;
    }

    public void dispose() {
        floatingComponentMap.clear();
        applicationConnection.disconnect();
    }
}
