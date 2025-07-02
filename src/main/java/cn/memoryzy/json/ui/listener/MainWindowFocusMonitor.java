package cn.memoryzy.json.ui.listener;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.DataFormatType;
import cn.memoryzy.json.event.HistoryAddedEvent;
import cn.memoryzy.json.event.RefreshFloatToolbarEvent;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.state.v2.EditorBehaviorState;
import cn.memoryzy.json.service.persistent.state.v2.HistoryState;
import cn.memoryzy.json.service.persistent.state.v2.JsonRecord;
import cn.memoryzy.json.service.persistent.v2.HistoryManager;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.FocusChangeListener;
import com.intellij.util.Alarm;
import com.intellij.util.AlarmFactory;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/25
 */
public class MainWindowFocusMonitor implements FocusChangeListener, Disposable {

    /**
     * 最大字符数量
     */
    public static final int THRESHOLD = 3000;

    /**
     * 5秒延迟保存
     */
    private static final int SAVE_DELAY = 5000;

    private final EditorBehaviorState behaviorState;
    private final HistoryState historyState;
    private final HistoryManager historyManager;
    private final Alarm alarm;

    public MainWindowFocusMonitor(EditorBehaviorState behaviorState, HistoryState historyState, HistoryManager historyManager) {
        this.behaviorState = behaviorState;
        this.historyState = historyState;
        this.historyManager = historyManager;
        this.alarm = AlarmFactory.getInstance().create(Alarm.ThreadToUse.POOLED_THREAD, this);
    }

    @Override
    public void focusGained(@NotNull Editor editor) {
        if (isAutoHistoryEnabled()) {
            // 获取焦点时，取消之前的任务
            cancelPendingSave();
        }

        // ----------- 处理剪贴板数据
        // 配置允许了，并且当前编辑器内容为空
        if (behaviorState.isAutoRecognizeFormats() && StrUtil.isBlank(editor.getDocument().getText())) {
            // 处理剪贴板数据
            processClipboardContent(editor);
        }
    }

    @Override
    public void focusLost(@NotNull Editor editor) {
        if (isAutoHistoryEnabled()) {
            // 添加当前编辑器的 JSON 至历史记录
            scheduleDelayedSave(editor);
        }
    }


    private void processClipboardContent(Editor editor) {
        String clipboard = StrUtil.trim(PlatformUtil.getClipboard());
        if (StrUtil.isNotBlank(clipboard)) {
            if (clipboard.length() > THRESHOLD) {
                // 异步处理大文本
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    processClipboardContent(editor, clipboard);
                });
            } else {
                processClipboardContent(editor, clipboard);
            }
        }
    }

    private void processClipboardContent(Editor editor, String clipboard) {
        // 检查编辑器是否仍然有效
        if (editor.isDisposed()) return;
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        messageBus.syncPublisher(RefreshFloatToolbarEvent.TOPIC).accept(editor, clipboard);
    }

    private void scheduleDelayedSave(@NotNull Editor editor) {
        // 取消之前所有的保存请求
        cancelPendingSave();
        // 添加新的延迟保存请求
        alarm.addRequest(() -> automaticallySaveHistory(editor), SAVE_DELAY);
    }

    /**
     * 自动保存历史记录
     *
     * @param editor 编辑器
     */
    private void automaticallySaveHistory(@NotNull Editor editor) {
        // 检查编辑器是否仍然有效
        if (editor.isDisposed()) return;
        // 获取编辑器内容
        String content = StrUtil.trim(editor.getDocument().getText());
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

        // 自动保存的话，无需指定名称
        historyManager.addEntry(new JsonRecord().setRawText(content).setSourceType(formatType).setWrapper(wrapper));
        // 触发事件
        ApplicationManager.getApplication().getMessageBus().syncPublisher(HistoryAddedEvent.TOPIC).added();
    }

    /**
     * 是否启用自动记录历史记录
     *
     * @return true：启用；false：
     */
    private boolean isAutoHistoryEnabled() {
        return historyState.isEnableHistory() && historyState.isAutoRecordHistory();
    }

    /**
     * 取消所有挂起的请求
     */
    private void cancelPendingSave() {
        alarm.cancelAllRequests();
    }

    @Override
    public void dispose() {
        cancelPendingSave();
    }
}
