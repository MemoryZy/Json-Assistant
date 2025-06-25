package cn.memoryzy.json.ui.listener;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.event.RefreshFloatToolbarEvent;
import cn.memoryzy.json.service.persistent.state.v2.EditorBehaviorState;
import cn.memoryzy.json.service.persistent.state.v2.HistoryState;
import cn.memoryzy.json.service.persistent.v2.HistoryManager;
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

    public static final int THRESHOLD = 3000;

    private final EditorBehaviorState behaviorState;
    private final HistoryState historyState;
    private final HistoryManager historyManager;
    private final Alarm alarm;


    // TODO 这里待完善

    private static final int SAVE_DELAY = 5000; // 5秒延迟保存

    public MainWindowFocusMonitor(EditorBehaviorState behaviorState, HistoryState historyState, HistoryManager historyManager) {
        this.behaviorState = behaviorState;
        this.historyState = historyState;
        this.historyManager = historyManager;
        this.alarm = AlarmFactory.getInstance().create(Alarm.ThreadToUse.POOLED_THREAD, this);
    }

    @Override
    public void focusGained(@NotNull Editor editor) {
        // 配置允许了，并且当前编辑器内容为空
        if (behaviorState.isAutoRecognizeFormats() && StrUtil.isBlank(editor.getDocument().getText())) {
            // 处理剪贴板数据
            processClipboardContent(editor);
        }
    }

    @Override
    public void focusLost(@NotNull Editor editor) {
        // 添加当前编辑器的 JSON 至历史记录
        if (historyState.isEnableHistory() && historyState.isAutoRecordHistory()) {
            scheduleDelayedHistoryCapture();



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
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        messageBus.syncPublisher(RefreshFloatToolbarEvent.TOPIC).accept(editor, clipboard);
    }

    private void scheduleDelayedHistoryCapture() {

    }

    @Override
    public void dispose() {
        alarm.cancelAllRequests();
    }
}
