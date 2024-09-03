package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.LanguageTextField;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/21
 */
public class SaveToDiskAction extends DumbAwareAction implements UpdateInBackground {

    private final LanguageTextField languageTextField;

    public SaveToDiskAction(LanguageTextField languageTextField) {
        super();
        this.languageTextField = languageTextField;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.save.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.save.json.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.SAVE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        FileChooserFactory chooserFactory = FileChooserFactory.getInstance();
        FileSaverDescriptor saverDescriptor = new FileSaverDescriptor(
                JsonAssistantBundle.messageOnSystem("dialog.file.save.json.title"),
                "",
                "json");
        FileSaverDialog saverDialog = chooserFactory.createSaveFileDialog(saverDescriptor, project);
        VirtualFileWrapper virtualFileWrapper = saverDialog.save("export.json");

        if (Objects.nonNull(virtualFileWrapper)) {
            String text = StrUtil.trim(languageTextField.getText());
            String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);

            File file = virtualFileWrapper.getFile();
            FileUtil.writeUtf8String(jsonStr, file);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null && JsonAssistantUtil.isJsonOrExtract(languageTextField.getText()));
    }
}
