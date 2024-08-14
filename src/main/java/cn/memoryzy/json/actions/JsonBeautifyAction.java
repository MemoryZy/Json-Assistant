package cn.memoryzy.json.actions;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.models.formats.JsonFormatHandleModel;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.JsonUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.json.JsonFileType;
import com.intellij.json.json5.Json5FileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

        JsonAssistantUtil.writeOrCopyJsonOnEditor(project, editor, document, formattedJson, model, true, isNotWriteDoc(project, document, model));
    }

    /**
     * 是否在当前文档内写入；true，不写；false：写入
     */
    public static boolean isNotWriteDoc(Project project, Document document, JsonFormatHandleModel model) {
        // 是否在当前文档内写入；true，不写；false：写入
        boolean isNotWriteDoc;
        if (document.isWritable()) {
            if (model.getSelectedText()) {
                isNotWriteDoc = false;
            } else {
                FileType fileType = PlatformUtil.getDocumentFileType(project, document);
                // 如果选中了，那么在选中内写入
                isNotWriteDoc = !Objects.equals(fileType, JsonFileType.INSTANCE) && !Objects.equals(fileType, Json5FileType.INSTANCE);
            }
        } else {
            isNotWriteDoc = true;
        }

        return isNotWriteDoc;
    }
}
