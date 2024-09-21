package cn.memoryzy.json.action;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.model.formats.JsonFormatHandleModel;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/6/21
 */
public class JsonBeautifyAction extends DumbAwareAction {

    private static final Logger LOG = Logger.getInstance(JsonBeautifyAction.class);

    public JsonBeautifyAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.beautify.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.beautify.description"));
        presentation.setIcon(JsonAssistantIcons.DIZZY_STAR);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = PlatformUtil.getEditor(e);
        handleJsonBeautify(e, editor);
    }

    public static void handleJsonBeautify(AnActionEvent e, Editor editor) {
        Project project = e.getProject();
        Document document = editor.getDocument();
        JsonFormatHandleModel model = JsonFormatHandleModel.of(project, editor,
                JsonAssistantBundle.messageOnSystem("hint.select.json.beautify.text"),
                JsonAssistantBundle.messageOnSystem("hint.all.json.beautify.text"));

        String formattedJson;
        try {
            formattedJson = StrUtil.trim(JsonUtil.formatJson(model.getContent()));
        } catch (Exception ex) {
            LOG.error("Json format error", ex);
            return;
        }

        JsonAssistantUtil.applyProcessedTextToDocumentOrClipboard(
                project, editor, document, formattedJson, model, true,
                JsonAssistantUtil.isNotWriteJsonDoc(e, project, document, model),
                FileTypeHolder.JSON);
    }

}
