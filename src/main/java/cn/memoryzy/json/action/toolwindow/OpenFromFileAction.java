package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.ArrayUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.extension.file.ExternalFileWrapper;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.ToolWindowUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Memory
 * @since 2025/7/10
 */
public class OpenFromFileAction extends DumbAwareAction implements UpdateInBackground {

    public static final Key<Boolean> EXTERNAL_FILE_MARKER =
            Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".EXTERNAL_FILE_WRITE_ACCESS");

    public OpenFromFileAction() {
        super(JsonAssistantBundle.messageOnSystem("action.openFromFile.text"), JsonAssistantBundle.messageOnSystem("action.openFromFile.description"), JsonAssistantIcons.ToolWindow.IMPORT);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        if (project == null) return;

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
        String content = PlatformUtil.getFileContent(selectFile);
        if (!JsonUtil.isJson(content) && !Json5Util.isJson5(content)) {
            ToolWindowManager.getInstance(project).notifyByBalloon(
                    PluginConstant.JSON_ASSISTANT_TOOLWINDOW_ID,
                    MessageType.WARNING,
                    JsonAssistantBundle.messageOnSystem("hint.select.json.content"));
            return;
        }

        try {
            selectFile.setWritable(true);
        } catch (IOException ignored) {
        }

        selectFile.putUserData(EXTERNAL_FILE_MARKER, true);

        // 获取当前选择的窗口页，判断其是否存在内容，若存在，则新开标签页
        openSelectedTabIfContentExists(project, selectFile);
    }

    private void openSelectedTabIfContentExists(Project project, VirtualFile selectFile) {
        ToolWindowEx toolWindow = (ToolWindowEx) ToolWindowUtil.getJsonAssistantToolWindow(project);
        // 用包装类代替 VirtualFile
        Content content = ToolWindowUtil.addNewContent(project, toolWindow, ContentFactory.SERVICE.getInstance(), new ExternalFileWrapper(selectFile));
        content.setDisplayName(JsonAssistantBundle.messageOnSystem("toolwindow.tab.import.name"));
    }
}
