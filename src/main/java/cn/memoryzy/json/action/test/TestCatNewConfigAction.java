package cn.memoryzy.json.action.test;

import cn.memoryzy.json.service.persistent.state.v2.JsonRecord;
import cn.memoryzy.json.service.persistent.v2.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
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
        BlacklistManager blacklistManager = BlacklistManager.getInstance();

        GeneralSettings generalSettings = GeneralSettings.getInstance();

        HistoryManager historyManager = HistoryManager.getInstance(getEventProject(e));

        SerializationSettings serializationSettings = SerializationSettings.getInstance();

        ToolWindowSettings toolWindowSettings = ToolWindowSettings.getInstance();

        JsonRecord jsonRecord = new JsonRecord()
                .setId(123);

        historyManager.addEntry(jsonRecord);

        System.out.println();
    }
}
