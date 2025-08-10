package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.extension.file.ExternalFileWrapper;
import cn.memoryzy.json.service.persistent.ToolWindowSettings;
import cn.memoryzy.json.service.persistent.state.EditorBehaviorState;
import cn.memoryzy.json.ui.panel.CombineCardLayout;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.ToolWindowUtil;
import com.intellij.ide.HelpTooltip;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

/**
 * @author Memory
 * @since 2025/7/10
 */
public class OpenFromFileAction extends DumbAwareAction implements CustomComponentAction, UpdateInBackground {

    public static final Key<Boolean> EXTERNAL_FILE_MARKER =
            Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".EXTERNAL_FILE_WRITE_ACCESS");

    public static final Key<ExternalFileWrapper> EXTERNAL_FILE_CACHED =
            Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".EXTERNAL_FILE_CACHED");

    private final CombineCardLayout cardLayout;
    private final EditorBehaviorState editorBehaviorState;

    public OpenFromFileAction(CombineCardLayout cardLayout) {
        super(JsonAssistantBundle.messageOnSystem("action.openFromFile.text"), JsonAssistantBundle.messageOnSystem("action.openFromFile.description"), JsonAssistantIcons.ToolWindow.IMPORT);
        this.cardLayout = cardLayout;
        this.editorBehaviorState = ToolWindowSettings.getInstance().getBehaviorState();
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        ActionButton button = new ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE) {
            @Override
            protected void updateToolTipText() {
                HelpTooltip.dispose(this);
                // noinspection DialogTitleCapitalization
                new HelpTooltip()
                        .setTitle(getTemplatePresentation().getText())
                        .setDescription(JsonAssistantBundle.messageOnSystem("tooltip.open.file.text"))
                        .installOn(this);
            }
        };

        button.setBorder(JBUI.Borders.empty(1, 2));
        return button;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        if (project == null) return;

        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);

        // 打开文件选择弹窗
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, true, false);
        descriptor.withTitle(JsonAssistantBundle.messageOnSystem("dialog.chooser.jsonFile.title"));
        // 过滤文件类型，只允许选择 JSON/JSON5 文件（选择其他文件不方便同时处理自动保存和高亮语法支持）
        // descriptor.withFileFilter(file -> StrUtil.equalsIgnoreCase("json", file.getExtension()) || StrUtil.equalsIgnoreCase("json5", file.getExtension()));

        FileChooserDialog fileChooser = FileChooserFactory.getInstance().createFileChooser(descriptor, project, null);
        VirtualFile[] files = fileChooser.choose(project, (VirtualFile) null);
        if (ArrayUtil.isEmpty(files)) return;
        // 单选
        VirtualFile selectFile = files[0];

        // 验证此文件内容是否为 JSON 格式
        String content = PlatformUtil.getContentFromVirtualFile(selectFile);
        if (!JsonUtil.isJson(content) && !Json5Util.isJson5(content)) {
            windowManager.notifyByBalloon(
                    PluginConstant.JSON_ASSISTANT_TOOLWINDOW_ID,
                    MessageType.WARNING,
                    JsonAssistantBundle.messageOnSystem("hint.select.json.content"));
            return;
        }

        // 包装虚拟文件，以便后续操作（为了在设置变更后，能切换到可写入的真实文件）
        ExternalFileWrapper fileWrapper = new ExternalFileWrapper(selectFile);
        PlatformUtil.markVirtualFileWritable(fileWrapper);
        // 默认用实体文件
        selectFile = fileWrapper;

        // 如果选择修改不作用于源文件，那么只拷贝内容
        if (!editorBehaviorState.isShouldApplyToSource()) {
            // 使用 LightVirtualFile，只处理内容
            selectFile = PlatformUtil.createLightVirtualFile(PluginConstant.MAIN_WINDOW_DISPLAY_NAME, FileTypeHolder.JSON5, content);
        }

        // 获取当前选择的窗口页，判断其是否存在内容，若存在，则新开标签页
        openSelectedTabIfContentExists(project, null, selectFile, fileWrapper, null, editorBehaviorState.isShouldApplyToSource());

        // 在此增加一个提示（有次数限制），告知用户是否可以修改此设置
        showExternalFileReminder(project, windowManager);
    }

    private void showExternalFileReminder(Project project, ToolWindowManager windowManager) {
        PropertiesComponent component = PropertiesComponent.getInstance();
        // 次数
        int time = component.getInt(PluginConstant.EXTERNAL_FILE_REMINDER, 0);
        // 大于等于3次 或 今天已经提示过
        if (time >= 3) return;

        // 不同的提示
        String message = editorBehaviorState.isShouldApplyToSource()
                ? JsonAssistantBundle.messageOnSystem("hint.edit.mode.content")
                : JsonAssistantBundle.messageOnSystem("hint.safe.mode.content");

        windowManager.notifyByBalloon(PluginConstant.JSON_ASSISTANT_TOOLWINDOW_ID, MessageType.INFO, message, null, event -> {
            if (HyperlinkEvent.EventType.ACTIVATED == event.getEventType()) {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, JsonAssistantBundle.message("setting.display.name"));
            }
        });

        component.setValue(PluginConstant.EXTERNAL_FILE_REMINDER, time + 1, 0);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(cardLayout.isEditorView());
    }

    public static void openSelectedTabIfContentExists(Project project,
                                                      ToolWindowEx toolWindow,
                                                      VirtualFile selectFile,
                                                      ExternalFileWrapper fileWrapper,
                                                      String newText,
                                                      boolean shouldApplyToSource) {
        String tag = shouldApplyToSource ? JsonAssistantBundle.messageOnSystem("toolwindow.content.importData.sourceFile.tag") : JsonAssistantBundle.messageOnSystem("toolwindow.content.importData.copy.tag");
        if (null == toolWindow) toolWindow = (ToolWindowEx) ToolWindowUtil.getJsonAssistantToolWindow(project);
        Content content = ToolWindowUtil.addNewContent(project, toolWindow, ContentFactory.SERVICE.getInstance(), selectFile);
        content.setDisplayName(ToolWindowUtil.generateTagName(toolWindow.getContentManager(), JsonAssistantBundle.messageOnSystem("toolwindow.content.importData.title") + " (" + tag + ")"));

        // 补充标记
        EditorEx editor = ToolWindowUtil.getEditorOnContent(content);
        if (null == editor) return;
        if (null != fileWrapper) editor.putUserData(EXTERNAL_FILE_CACHED, fileWrapper);

        if (StrUtil.isNotBlank(newText)) {
            PlatformUtil.safeSetDocumentText(project, editor.getDocument(), newText);
        }
    }
}
