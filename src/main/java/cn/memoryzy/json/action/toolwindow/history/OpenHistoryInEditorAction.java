package cn.memoryzy.json.action.toolwindow.history;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.DumbAwareBaseAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.HistoryManager;
import cn.memoryzy.json.service.persistent.state.JsonRecord;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.ToolWindowUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/7/7
 */
public class OpenHistoryInEditorAction extends DumbAwareBaseAction {

    private final HistoryToolWindowComponentProvider provider;

    public OpenHistoryInEditorAction(HistoryToolWindowComponentProvider provider, ToolWindow toolWindow) {
        super(JsonAssistantBundle.messageOnSystem("action.openHistoryInEditor.text"), JsonAssistantBundle.messageOnSystem("action.openHistoryInEditor.description"), JsonAssistantIcons.ToolWindow.SEND);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt M"), toolWindow.getComponent());
        this.provider = provider;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        if (null == project) return;

        // 把 VirtualFile 文件对象传递过去
        VirtualFile currentFile = provider.getCurrentFile();
        if (null == currentFile) return;

        // 新建标签页
        ToolWindowEx toolWindow = (ToolWindowEx) ToolWindowUtil.getJsonAssistantWindow(project);
        Content content = ToolWindowUtil.addNewContent(project, toolWindow, ContentFactory.SERVICE.getInstance(), currentFile);

        // 改为记录名
        JsonRecord record = HistoryManager.getInstance(project).findRecord(currentFile.getNameWithoutExtension());
        content.setDisplayName(
                StrUtil.isNotBlank(record.getName())
                        ? record.getName()
                        : ToolWindowUtil.generateTagName(toolWindow.getContentManager(), PlatformUtil.isChineseLocale() ? "记录" : "Record"));

        // 打开窗口
        toolWindow.show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(getEventProject(e) != null && null != provider.getCurrentFile());
    }
}
