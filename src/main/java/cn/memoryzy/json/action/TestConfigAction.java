package cn.memoryzy.json.action;

import cn.memoryzy.json.enums.ColorScheme;
import cn.memoryzy.json.enums.DataFormatType;
import cn.memoryzy.json.service.persistent.state.v2.EditorAppearanceStateV2;
import cn.memoryzy.json.service.persistent.state.v2.EditorBehaviorStateV2;
import cn.memoryzy.json.service.persistent.v2.EditorAppearanceSettings;
import cn.memoryzy.json.service.persistent.v2.EditorBehaviorSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Memory
 * @since 2025/6/17
 */
public class TestConfigAction extends DumbAwareAction implements UpdateInBackground {

    public TestConfigAction() {
        super("配置测试");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // ToolWindowState state = ToolWindowSettings.getInstance(e.getProject()).getState();
        //
        // state.appearanceState.setColorScheme(ColorScheme.Blue.name());
        //
        // AnnouncementStatsV2 announcementStats = new AnnouncementStatsV2();
        // announcementStats.displayCount = 2;
        //
        // state.stats.add(announcementStats);
        //
        // AnnouncementStatsV2 announcementStats1 = new AnnouncementStatsV2();
        // announcementStats1.lastShownTime = System.currentTimeMillis();
        // state.statsMap.put("ss", announcementStats1);
        //
        //
        // state.getHistory().add("xxx12");
        // state.getHistory().add("你好");

        Project project = e.getProject();

        EditorAppearanceStateV2 state = EditorAppearanceSettings.getInstance(project).getState();

        state.setColorScheme(ColorScheme.Green.name());
        state.setShowFoldingOutline(true);

        EditorBehaviorStateV2 state1 = EditorBehaviorSettings.getInstance(project).getState();
        state1.setEnabledFormats(List.of(DataFormatType.TOML.name(), DataFormatType.XML.name()));

        state1.setShouldPromptBeforeImport(true);


    }
}
