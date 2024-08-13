package cn.memoryzy.json.actions;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.models.formats.JsonFormatHandleModel;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.JsonUtil;
import cn.memoryzy.json.utils.PlatformUtil;
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
        Project project = e.getProject();
        Editor editor = PlatformUtil.getEditor(e);
        Document document = editor.getDocument();
        JsonFormatHandleModel model = JsonFormatHandleModel.of(editor,
                JsonAssistantBundle.messageOnSystem("hint.select.json.beautify.text"),
                JsonAssistantBundle.messageOnSystem("hint.all.json.beautify.text"));

        String formattedJson;
        try {
            formattedJson = StrUtil.trim(JsonUtil.formatJson(model.getContent()));
        } catch (Exception ex) {
            LOG.error("Json format error", ex);
            return;
        }

        // TODO 在非 JSON 的文件中，不修改全局文本
        Boolean selectedText = model.getSelectedText();

        JsonAssistantUtil.writeOrCopyJsonOnEditor(project, editor, document, formattedJson, model, true, false);
    }

}
