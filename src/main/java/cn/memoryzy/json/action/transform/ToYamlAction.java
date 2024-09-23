package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.model.formats.JsonFormatHandleModel;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.YamlUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/9/19
 */
public class ToYamlAction extends DumbAwareAction {

    private static final Logger LOG = Logger.getInstance(ToYamlAction.class);

    public ToYamlAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.to.yaml.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.to.yaml.description"));
        presentation.setIcon(AllIcons.FileTypes.Yaml);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = PlatformUtil.getEditor(e);
        Document document = editor.getDocument();
        JsonFormatHandleModel model = JsonFormatHandleModel.of(project, editor,
                JsonAssistantBundle.messageOnSystem("hint.select.json.to.yaml.text"),
                JsonAssistantBundle.messageOnSystem("hint.all.json.to.yaml.text"));

        String yamlStr;
        try {
            yamlStr = YamlUtil.jsonToYaml(model.getContent());
        } catch (Exception ex) {
            LOG.error("Yaml conversion failure", ex);
            return;
        }

        JsonAssistantUtil.applyProcessedTextToDocumentOrClipboard(
                e.getProject(), editor, document, yamlStr,
                model, true, true, FileTypeHolder.YAML);
    }
}
