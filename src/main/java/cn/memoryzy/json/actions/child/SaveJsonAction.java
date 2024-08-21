package cn.memoryzy.json.actions.child;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/21
 */
public class SaveJsonAction extends DumbAwareAction implements UpdateInBackground {

    private final JsonViewerWindow window;

    public SaveJsonAction(JsonViewerWindow window) {
        super();
        this.window = window;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.save.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.save.json.description"));
        presentation.setIcon(JsonAssistantIcons.SAVE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        FileChooserFactory chooserFactory = FileChooserFactory.getInstance();
        FileSaverDescriptor saverDescriptor = new FileSaverDescriptor(
                JsonAssistantBundle.messageOnSystem("dialog.file.save.json.title"),
                JsonAssistantBundle.messageOnSystem("dialog.file.save.json.description"),
                "json");
        FileSaverDialog saverDialog = chooserFactory.createSaveFileDialog(saverDescriptor, project);
        VirtualFileWrapper virtualFileWrapper = saverDialog.save("export.json");

        if (Objects.nonNull(virtualFileWrapper)) {
            String text = StrUtil.trim(window.getJsonContent());
            String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);

            File file = virtualFileWrapper.getFile();
            FileUtil.writeUtf8String(jsonStr, file);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        String text = StrUtil.trim(window.getJsonContent());
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);
        e.getPresentation().setEnabled(project != null && StrUtil.isNotBlank(jsonStr));
    }
}
