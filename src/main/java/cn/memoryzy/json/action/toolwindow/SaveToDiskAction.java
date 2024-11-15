package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.ex.EditorEx;
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
public class SaveToDiskAction extends DumbAwareAction implements UpdateInBackground {

    private final EditorEx editor;

    public SaveToDiskAction(EditorEx editor) {
        super();
        this.editor = editor;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.save.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.save.json.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.SAVE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        String docText = editor.getDocument().getText();
        String extension = JsonUtil.canResolveToJson(docText) ? "json" : "json5";
        FileChooserFactory chooserFactory = FileChooserFactory.getInstance();
        FileSaverDescriptor saverDescriptor = new FileSaverDescriptor(JsonAssistantBundle.messageOnSystem("dialog.file.save.json.title"), "", extension);
        FileSaverDialog saverDialog = chooserFactory.createSaveFileDialog(saverDescriptor, project);
        VirtualFileWrapper virtualFileWrapper = saverDialog.save("export." + extension);

        if (Objects.nonNull(virtualFileWrapper)) {
            String text = StrUtil.trim(editor.getDocument().getText());
            String jsonStr = JsonUtil.ensureJson(text);

            File file = virtualFileWrapper.getFile();
            FileUtil.writeUtf8String(jsonStr, file);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(GlobalJsonConverter.validateEditorAllJson(getEventProject(event), editor));
    }
}
