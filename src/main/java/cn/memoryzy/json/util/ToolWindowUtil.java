package cn.memoryzy.json.util;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.ui.JsonAssistantToolWindowComponentProvider;
import cn.memoryzy.json.ui.component.ToolWindowPanel;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.openapi.wm.impl.content.BaseLabel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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
        Content mainContent = getInitialContent(toolWindow);
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

    /**
     * 获取 Json Assistant 的工具窗口
     *
     * @param project 项目实例，用于确定工具窗口所属的项目
     * @return ToolWindow 返回找到或新创建的工具窗口实例
     */
    public static ToolWindow getJsonAssistantToolWindow(Project project) {
        return ToolWindowManager.getInstance(project).getToolWindow(PluginConstant.JSON_ASSISTANT_TOOLWINDOW_ID);
    }

    /**
     * 获取选定的选项卡，如果未选定任何选项卡，则返回第一个选项卡
     *
     * @param toolWindow 工具窗口对象
     * @return 当前选定的选项卡，如果未选定任何选项卡，则返回第一个选项卡
     */
    public static Content getSelectedContent(ToolWindow toolWindow) {
        if (toolWindow == null) return null;
        ContentManager contentManager = toolWindow.getContentManager();
        Content selectedContent = contentManager.getSelectedContent();
        if (Objects.isNull(selectedContent)) {
            selectedContent = contentManager.getContent(0);
        }

        return selectedContent;
    }

    /**
     * 获取工具窗口的初始选项卡
     *
     * @param toolWindow 工具窗口对象
     * @return 工具窗口的初始选项卡
     **/
    public static Content getInitialContent(ToolWindow toolWindow) {
        if (toolWindow == null) return null;
        ContentManager contentManager = toolWindow.getContentManager();
        return contentManager.getContent(0);
    }


    /**
     * 获取选项卡内的面板实例
     *
     * @param content 选项卡实例，从中获取面板实例
     * @return 组件面板
     */
    public static ToolWindowPanel getPanelOnContent(Content content) {
        if (Objects.nonNull(content)) {
            SimpleToolWindowPanel windowPanel = (SimpleToolWindowPanel) content.getComponent();
            return (ToolWindowPanel) windowPanel.getContent();
        }

        return null;
    }

    /**
     * 获取选项卡内的编辑器实例
     *
     * @param content 选项卡实例，从中获取编辑器实例
     * @return 编辑器
     */
    public static EditorEx getEditorOnContent(Content content) {
        ToolWindowPanel showPanel = getPanelOnContent(content);
        if (Objects.nonNull(showPanel)) {
            return showPanel.getEditor();
        }

        return null;
    }

    /**
     * 向指定的工具窗口中添加新选项卡
     *
     * @param project        当前的项目实例，用于创建窗口组件提供者
     * @param toolWindow     工具窗口实例，用于获取选项卡管理器
     * @param contentFactory 选项卡工厂实例，用于创建新选项卡
     * @param editorFileType 编辑器文件类型，用于创建窗口组件提供者
     * @return 返回创建并添加到工具窗口的新选项卡
     */
    public static Content addNewContent(Project project, ToolWindowEx toolWindow, ContentFactory contentFactory, FileType editorFileType) {
        ContentManager contentManager = toolWindow.getContentManager();
        int contentCount = contentManager.getContentCount();
        String displayName = PluginConstant.JSON_ASSISTANT_TOOL_WINDOW_DISPLAY_NAME + " " + (contentCount + 1);

        JsonAssistantToolWindowComponentProvider window = new JsonAssistantToolWindowComponentProvider(project, editorFileType, false);
        Content content = contentFactory.createContent(window.createRootPanel(), displayName, false);
        contentManager.addContent(content, contentCount);
        contentManager.setSelectedContent(content, true);
        return content;
    }

    /**
     * 通过当前处于焦点的组件获取选项卡对象，若未找到，则返回当前选定的选项卡
     *
     * @param dataContext 数据上下文
     * @return 选项卡对象
     */
    public static Content getContextContent(@NotNull DataContext dataContext, @NotNull ToolWindow toolWindow) {
        Content selectedContent = getContextContent(dataContext);
        if (selectedContent == null) {
            selectedContent = toolWindow.getContentManager().getSelectedContent();
        }
        return selectedContent;
    }

    /**
     * 通过当前处于焦点的组件获取选项卡对象
     *
     * @param dataContext 数据上下文
     * @return 选项卡对象
     */
    public static Content getContextContent(@NotNull DataContext dataContext) {
        BaseLabel baseLabel = ObjectUtils.tryCast(dataContext.getData(PlatformDataKeys.CONTEXT_COMPONENT), BaseLabel.class);
        return baseLabel != null ? baseLabel.getContent() : null;
    }

    public static JComponent getPrimaryComponentFromToolWindow(ToolWindow toolWindow) {
        if (Objects.nonNull(toolWindow)) {
            ContentManager contentManager = toolWindow.getContentManager();
            Content content = contentManager.getContent(0);
            if (Objects.nonNull(content)) {
                return content.getComponent();
            }
        }

        return null;
    }

}
