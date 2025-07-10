package cn.memoryzy.json.action.toolwindow.history;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
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
        ToolWindowEx toolWindow = (ToolWindowEx) ToolWindowUtil.getJsonAssistantToolWindow(project);
        Content content = ToolWindowUtil.getSelectedContent(toolWindow);
        JsonAssistantToolWindowPanel panelOnContent = ToolWindowUtil.getPanelOnContent(content);
        boolean hasText = Optional.ofNullable(panelOnContent)
                .map(JsonAssistantToolWindowPanel::getEditor)
                .map(EditorEx::getDocument)
                .map(document -> StrUtil.isNotBlank(document.getText()))
                .orElse(false);

        // 有文本，新开标签页
        if (hasText) {
            content = ToolWindowUtil.addNewContent(project, toolWindow, ContentFactory.SERVICE.getInstance(), FileTypeHolder.JSON5);
            panelOnContent = ToolWindowUtil.getPanelOnContent(content);
        }

        Optional.ofNullable(panelOnContent)
                .map(JsonAssistantToolWindowPanel::getEditor)
                .ifPresent(editor -> PlatformUtil.safeSetDocumentText(project, editor.getDocument(), recordText));

        // 打开窗口
        toolWindow.show();
    }
}
