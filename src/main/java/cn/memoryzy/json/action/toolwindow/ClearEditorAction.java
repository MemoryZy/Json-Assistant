package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.extension.file.ExternalFileWrapper;
import cn.memoryzy.json.ui.dialog.OkCancelDialog;
import cn.memoryzy.json.ui.panel.CombineCardLayout;
import cn.memoryzy.json.util.ToolWindowUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/21
 */
public class ClearEditorAction extends DumbAwareAction implements UpdateInBackground {
    public static final String DO_NOT_ASK_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".ClearEditorAction";

    private final EditorEx editor;
    private final Content content;
    private final ToolWindowEx toolWindow;
    private final CombineCardLayout cardLayout;
    private final VirtualFile sourceFile;

    public ClearEditorAction(EditorEx editor, Content content, ToolWindowEx toolWindow, CombineCardLayout cardLayout, VirtualFile sourceFile) {
        super();
        this.editor = editor;
        this.content = content;
        this.toolWindow = toolWindow;
        this.cardLayout = cardLayout;
        this.sourceFile = sourceFile;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.clear.editor.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.clear.editor.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.DELETE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        PropertiesComponent component = PropertiesComponent.getInstance();

        // 在实体文件时，加个提示，是否真的要清空内容
        if (sourceFile instanceof ExternalFileWrapper) {
            // 选择了默认执行
            if (component.getBoolean(DO_NOT_ASK_KEY, false)) {
                clearText(project);

            } else {
                if (new OkCancelDialog(
                        JsonAssistantBundle.messageOnSystem("dialog.clear.editor.title"),
                        JsonAssistantBundle.messageOnSystem("dialog.clear.editor.content"),
                        Messages.getWarningIcon())
                        .doNotAsk((isSelected, exitCode) -> {
                            // 点击确定，且选中了复选框
                            if (DialogWrapper.OK_EXIT_CODE == exitCode && isSelected) {
                                component.setValue(DO_NOT_ASK_KEY, true);
                            }
                        }).ask()) {

                    clearText(project);
                }
            }

        } else {
            clearText(project);
            // 清除标签名
            String displayName = content.getDisplayName();
            // 如果不属于默认名称，那就清除
            if (!ToolWindowUtil.isDefaultTabName(displayName)) {
                content.setDisplayName(ToolWindowUtil.generateTagName(toolWindow.getContentManager(), PluginConstant.MAIN_WINDOW_DISPLAY_NAME));
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(
                getEventProject(event) != null
                        && StrUtil.isNotBlank(editor.getDocument().getText())
                        && cardLayout.isEditorView());
    }

    private void clearText(Project project) {
        WriteCommandAction.runWriteCommandAction(project, () -> editor.getDocument().setText(""));
    }
}
