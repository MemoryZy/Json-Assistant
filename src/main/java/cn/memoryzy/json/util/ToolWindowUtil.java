package cn.memoryzy.json.util;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.ui.JsonAssistantToolWindowComponentProvider;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.openapi.wm.impl.content.BaseLabel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * @param tabName
     */
    public static void addNewContentWithEditorContentIfNeeded(Project project, String processedText, FileType editorFileType, String tabName) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ToolWindowEx toolWindow = (ToolWindowEx) getJsonAssistantToolWindow(project);
        Content mainContent = getInitialContent(toolWindow);
        EditorEx editor = getEditorOnContent(mainContent);

        if (StrUtil.isBlank(Objects.requireNonNull(editor).getDocument().getText())) {
            PlatformUtil.safeSetDocumentText(project, editor.getDocument(), processedText);

        } else {
            Content content = addNewContent(project, toolWindow, contentFactory, editorFileType, tabName);
            EditorEx editorEx = getEditorOnContent(content);
            PlatformUtil.safeSetDocumentText(project, Objects.requireNonNull(editorEx).getDocument(), processedText);
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
    public static JsonAssistantToolWindowPanel getPanelOnContent(Content content) {
        if (Objects.nonNull(content)) {
            SimpleToolWindowPanel windowPanel = (SimpleToolWindowPanel) content.getComponent();
            return (JsonAssistantToolWindowPanel) windowPanel.getContent();
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
        JsonAssistantToolWindowPanel showPanel = getPanelOnContent(content);
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
    public static Content addNewContent(Project project, ToolWindowEx toolWindow, ContentFactory contentFactory, FileType editorFileType, String tabName) {
        ContentManager contentManager = toolWindow.getContentManager();
        int contentCount = contentManager.getContentCount();

        String presetName = StrUtil.isNotBlank(tabName) ? tabName : PluginConstant.MAIN_WINDOW_DISPLAY_NAME;
        String displayName = generateTagName(contentManager, presetName);
        Content content = contentFactory.createContent(null, displayName, false);
        JsonAssistantToolWindowComponentProvider provider = new JsonAssistantToolWindowComponentProvider(project, toolWindow, content, editorFileType);

        content.setComponent(provider.createComponent());
        content.setPreferredFocusableComponent(provider.getPreferredFocusedComponent());
        content.setDisposer(provider);
        contentManager.addContent(content, contentCount);
        contentManager.setSelectedContent(content, true);
        return content;
    }

    /**
     * 向指定的工具窗口中添加新选项卡
     *
     * @param project        当前的项目实例，用于创建窗口组件提供者
     * @param toolWindow     工具窗口实例，用于获取选项卡管理器
     * @param contentFactory 选项卡工厂实例，用于创建新选项卡
     * @param sourceFile     原文件
     * @return 返回创建并添加到工具窗口的新选项卡
     */
    public static Content addNewContent(Project project, ToolWindowEx toolWindow, ContentFactory contentFactory, VirtualFile sourceFile) {
        ContentManager contentManager = toolWindow.getContentManager();
        int contentCount = contentManager.getContentCount();

        String displayName = generateTagName(contentManager, PluginConstant.MAIN_WINDOW_DISPLAY_NAME);
        Content content = contentFactory.createContent(null, displayName, false);
        JsonAssistantToolWindowComponentProvider window = new JsonAssistantToolWindowComponentProvider(project, toolWindow, content, sourceFile);

        content.setComponent(window.createComponent());
        content.setDisposer(window);
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


    /**
     * 将窗口移至右上角位置
     *
     * @param toolWindow 窗口
     */
    public static void moveWindowToRightTop(ToolWindow toolWindow) {
        toolWindow.setAnchor(ToolWindowAnchor.RIGHT, null);
        toolWindow.setSplitMode(false, null);
    }

    /**
     * 将窗口移至右下角位置
     *
     * @param toolWindow 窗口
     */
    public static void moveWindowToRightBottom(ToolWindow toolWindow) {
        toolWindow.setAnchor(ToolWindowAnchor.RIGHT, null);
        toolWindow.setSplitMode(true, null);
    }

    /**
     * 将窗口移至右下角位置
     *
     * @param toolWindow 窗口
     */
    public static void moveWindowToBottomRight(ToolWindow toolWindow) {
        toolWindow.setAnchor(ToolWindowAnchor.BOTTOM, null);
        toolWindow.setSplitMode(true, null);
    }


    /**
     * 目前此方法用于处理最后一个标签页关闭的情况
     */
    public static Disposable createAuxWindowContentDisposer(Project project, ToolWindow toolWindow) {
        return () -> {
            // 当最后一个标签页关闭，工具窗口暂时隐藏
            ContentManager contentManager = toolWindow.getContentManager();
            int contentCount = contentManager.getContentCount();
            if (contentCount == 0) {
                toolWindow.setAvailable(false);
                // 当辅助窗口被关闭时，再将Json 编辑器窗口移回右下角
                ToolWindowUtil.moveWindowToRightBottom(ToolWindowUtil.getJsonAssistantToolWindow(project));
            }
        };
    }


    /**
     * 生成新的标签名
     *
     * @param contentManager 内容管理
     * @param presetName     预设的标签名
     * @return 新生成的标签名
     */
    public static String generateTagName(ContentManager contentManager, String presetName) {
        List<String> tagList = Arrays.stream(contentManager.getContents())
                .map(Content::getDisplayName)
                .collect(Collectors.toList());
        return generateTagName(tagList, presetName);
    }


    /**
     * 生成新的标签名
     *
     * @param tagList    现有的标签名称列表
     * @param presetName 预设的标签名
     * @return 新生成的标签名
     */
    public static String generateTagName(List<String> tagList, String presetName) {
        // 检查预设名是否存在于标签列表中
        if (!tagList.contains(presetName)) {
            return presetName;
        }

        // 如果存在，找出所有以预设名开头且后面跟着数字的标签
        int maxNum = 0;
        String prefix = presetName + " ";
        int prefixLength = prefix.length();
        boolean hasNumberedTags = false;

        for (String tag : tagList) {
            if (tag.startsWith(prefix)) {
                // 提取数字部分
                String numPart = tag.substring(prefixLength);
                try {
                    int num = Integer.parseInt(numPart);
                    hasNumberedTags = true;
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException e) {
                    // 如果不是数字，忽略这个标签
                }
            }
        }

        // 如果没有找到带数字的标签，返回"预设名 1"
        // 否则返回"预设名 (maxNum + 1)"
        return hasNumberedTags ? presetName + " " + (maxNum + 1) : presetName + " 1";
    }

    /**
     * 检查输入的字符串是否符合"View"后跟数字的格式
     *
     * @param input 要检查的字符串
     * @return 如果符合格式返回true，否则返回false
     */
    public static boolean isDefaultTabName(String input) {
        if (StrUtil.isBlank(input)) {
            return false;
        }

        // 使用正则表达式匹配"View"后跟一个或多个数字的模式
        // ^表示字符串开始，$表示字符串结束，\\s+表示一个或多个空白字符，\\d+表示一个或多个数字
        return PluginConstant.MAIN_WINDOW_DISPLAY_NAME.equals(input) || input.matches("^View\\s+\\d+$");
    }

}
