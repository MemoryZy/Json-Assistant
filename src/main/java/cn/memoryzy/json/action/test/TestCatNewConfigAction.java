package cn.memoryzy.json.action.test;

import cn.memoryzy.json.enums.ColorScheme;
import cn.memoryzy.json.event.ColorSchemeChangedEvent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/23
 */
public class TestCatNewConfigAction extends DumbAwareAction implements UpdateInBackground {

    public TestCatNewConfigAction() {
        super("测试配置查看");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
//        BlacklistManager blacklistManager = BlacklistManager.getInstance();
//
//        GeneralSettings generalSettings = GeneralSettings.getInstance();
//
//        HistoryManager historyManager = HistoryManager.getInstance(getEventProject(e));
//
//        SerializationSettings serializationSettings = SerializationSettings.getInstance();
//
//        ToolWindowSettings toolWindowSettings = ToolWindowSettings.getInstance();
//
//        JsonRecord jsonRecord = new JsonRecord()
//                .setId(123);
//
//        historyManager.addEntry(jsonRecord);

//        MessageBus messageBus = e.getProject().getMessageBus();
//
//        RefreshFloatToolbarEvent refreshFloatToolbarEvent = messageBus.syncPublisher(RefreshFloatToolbarEvent.TOPIC);
//
//        System.out.println("["  + Thread.currentThread().getName() + "] send");
//
//        refreshFloatToolbarEvent.accept(null);

        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();

        ColorSchemeChangedEvent colorSchemeChangedEvent = messageBus.syncPublisher(ColorSchemeChangedEvent.TOPIC);

        colorSchemeChangedEvent.change(ColorScheme.Classic);



        System.out.println();
    }
}
