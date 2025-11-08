package cn.memoryzy.json.action.toolwindow.history;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.service.persistent.HistoryManager;
import cn.memoryzy.json.service.persistent.state.JsonRecord;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.ToolWindowUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author Memory
 * @since 2025/7/7
 */
public class OpenHistoryInEditorAction extends DumbAwareAction implements UpdateInBackground {

    public OpenHistoryInEditorAction() {
        super(JsonAssistantBundle.messageOnSystem("action.openHistoryInEditor.text"), JsonAssistantBundle.messageOnSystem("action.openHistoryInEditor.description"), JsonAssistantIcons.ToolWindow.SEND);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor recordEditor = PlatformUtil.getEditor(e.getDataContext());
        String recordText = recordEditor.getDocument().getText();
        if (StrUtil.isBlank(recordText)) return;

        // 如果当前页存在内容，则新开标签页
        Project project = getEventProject(e);
        if (null == project) return;

        ToolWindowEx toolWindow = (ToolWindowEx) ToolWindowUtil.getJsonAssistantWindow(project);
        Content content = ToolWindowUtil.getSelectedContent(toolWindow);
        JsonAssistantToolWindowPanel panelOnContent = ToolWindowUtil.getPanelOnContent(content);
        boolean hasText = Optional.ofNullable(panelOnContent)
                .map(JsonAssistantToolWindowPanel::getEditor)
                .map(EditorEx::getDocument)
                .map(document -> StrUtil.isNotBlank(document.getText()))
                .orElse(false);

        // 有文本，新开标签页
        if (hasText) {
            content = ToolWindowUtil.addNewContent(project, toolWindow, ContentFactory.SERVICE.getInstance(), FileTypeHolder.JSON5, null);
            panelOnContent = ToolWindowUtil.getPanelOnContent(content);
        }

        Optional.ofNullable(panelOnContent)
                .map(JsonAssistantToolWindowPanel::getEditor)
                .ifPresent(editor -> PlatformUtil.safeSetDocumentText(project, editor.getDocument(), recordText));

        // 设置标签页名称为记录名（因为只能在非编辑模式下点击此程序，所以可以通过文本找到记录）
        JsonRecord record = HistoryManager.getInstance(project).find(recordText);
        if (null != record) {
            String displayName = StrUtil.isNotBlank(record.getName())
                    ? record.getName()
                    : ToolWindowUtil.generateTagName(toolWindow.getContentManager(), PlatformUtil.isChineseLocale() ? "记录" : "Record");

            content.setDisplayName(displayName);
        }

        // 打开窗口
        toolWindow.show();
    }
}
