package cn.memoryzy.json.util;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.ui.JsonAssistantToolWindowComponentProvider;
import cn.memoryzy.json.ui.component.JsonAssistantToolWindowPanel;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/11/4
 */
public class ToolWindowUtil {

    /**
     * 根据需要添加新内容或更新编辑器内容<br/>
     * 此方法用于在工具窗口中添加新的内容或更新现有内容，具体取决于编辑器当前是否为空<br/>
     * 如果编辑器内容为空，则直接更新编辑器内容；如果不为空，则创建并添加新的内容<br/>
     *
     * @param project        当前项目实例，用于获取工具窗口和执行写入操作
     * @param processedText  要添加或更新的文本内容
     * @param editorFileType 编辑器文件类型，用于创建新内容时指定文件类型
     */
    public static void addNewContentWithEditorContentIfNeeded(Project project, String processedText, FileType editorFileType) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ToolWindowEx toolWindow = (ToolWindowEx) getJsonAssistantToolWindow(project);
        Content mainContent = getMainContent(toolWindow);
        EditorEx editor = getEditorOnContent(mainContent);

        if (StrUtil.isBlank(Objects.requireNonNull(editor).getDocument().getText())) {
            WriteCommandAction.runWriteCommandAction(project, () -> editor.getDocument().setText(processedText));
        } else {
            Content content = addNewContent(project, toolWindow, contentFactory, editorFileType);
            EditorEx editorEx = getEditorOnContent(content);
            WriteCommandAction.runWriteCommandAction(project, () -> Objects.requireNonNull(editorEx).getDocument().setText(processedText));
        }

        toolWindow.show();
    }


    public static ToolWindow getJsonAssistantToolWindow(Project project) {
        return ToolWindowManager.getInstance(project).getToolWindow(PluginConstant.JSON_VIEWER_TOOLWINDOW_ID);
    }


    public static Content getSelectedContent(ToolWindow toolWindow) {
        if (toolWindow == null) return null;
        ContentManager contentManager = toolWindow.getContentManager();
        Content selectedContent = contentManager.getSelectedContent();
        if (Objects.isNull(selectedContent)) {
            selectedContent = contentManager.getContent(0);
        }

        return selectedContent;
    }

    public static Content getMainContent(ToolWindow toolWindow) {
        if (toolWindow == null) return null;
        ContentManager contentManager = toolWindow.getContentManager();
        return contentManager.getContent(0);
    }


    public static JsonAssistantToolWindowPanel getPanelOnContent(Content content) {
        if (Objects.nonNull(content)) {
            SimpleToolWindowPanel windowPanel = (SimpleToolWindowPanel) content.getComponent();
            return (JsonAssistantToolWindowPanel) windowPanel.getContent();
        }

        return null;
    }

    public static EditorEx getEditorOnContent(Content content) {
        JsonAssistantToolWindowPanel viewerPanel = getPanelOnContent(content);
        if (Objects.nonNull(viewerPanel)) {
            return viewerPanel.getEditor();
        }

        return null;
    }

    public static Content addNewContent(Project project, ToolWindowEx toolWindow, ContentFactory contentFactory, FileType editorFileType) {
        ContentManager contentManager = toolWindow.getContentManager();
        int contentCount = contentManager.getContentCount();
        String displayName = PluginConstant.JSON_VIEWER_TOOL_WINDOW_DISPLAY_NAME + " " + (contentCount + 1);

        JsonAssistantToolWindowComponentProvider window = new JsonAssistantToolWindowComponentProvider(project, editorFileType, false);
        Content content = contentFactory.createContent(window.createRootPanel(), displayName, false);
        contentManager.addContent(content, contentCount);
        contentManager.setSelectedContent(content, true);
        return content;
    }


}
