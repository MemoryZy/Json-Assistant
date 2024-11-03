package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.model.formats.JsonFormatHandleModel;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TomlUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/9/25
 */
public class ToTomlAction extends DumbAwareAction implements UpdateInBackground {

    private static final Logger LOG = Logger.getInstance(ToTomlAction.class);

    public ToTomlAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.to.toml.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.to.toml.description"));
        presentation.setIcon(JsonAssistantIcons.FileTypes.TOML);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = PlatformUtil.getEditor(e);
        Document document = editor.getDocument();
        JsonFormatHandleModel model = JsonFormatHandleModel.of(project, editor,
                JsonAssistantBundle.messageOnSystem("hint.selection.json.to.toml.text"),
                JsonAssistantBundle.messageOnSystem("hint.global.json.to.toml.text"));

        String tomlStr;
        try {
            tomlStr = TomlUtil.toToml(model.getContent());
        } catch (Exception ex) {
            LOG.error("Toml conversion failure", ex);
            return;
        }

        JsonAssistantUtil.applyProcessedTextToDocumentOrClipboard(
                e.getProject(), editor, document, tomlStr,
                model, true, true, FileTypeHolder.TOML);
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        JsonFormatHandleModel model = JsonFormatHandleModel.of(getEventProject(e), PlatformUtil.getEditor(e));
        e.getPresentation().setEnabled(TomlUtil.canConvertToToml(model.getContent()));
    }
}
