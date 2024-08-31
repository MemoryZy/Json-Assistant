package cn.memoryzy.json.action;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.formats.JsonFormatHandleModel;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/14
 */
public class JsonTextDiffAction extends DumbAwareAction {

    public JsonTextDiffAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.text.diff.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.text.diff.description"));
        presentation.setIcon(JsonAssistantIcons.DIFF);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        Editor editor = PlatformUtil.getEditor(e);
        JsonFormatHandleModel model = JsonFormatHandleModel.of(editor);

        String leftJsonStr;
        try {
            leftJsonStr = StrUtil.trim(JsonUtil.formatJson(model.getContent()));
        } catch (Exception ex) {
            leftJsonStr = "";
        }

        DocumentContent leftDocumentContent = DiffContentFactory.getInstance().createEditable(project, leftJsonStr, JsonFileType.INSTANCE);
        DocumentContent rightDocumentContent = DiffContentFactory.getInstance().createEditable(project, "", JsonFileType.INSTANCE);

        SimpleDiffRequest simpleDiffRequest = new SimpleDiffRequest(
                JsonAssistantBundle.messageOnSystem("dialog.json.text.diff.main.title"),
                leftDocumentContent,
                rightDocumentContent,
                JsonAssistantBundle.messageOnSystem("dialog.json.text.diff.left.title"),
                JsonAssistantBundle.messageOnSystem("dialog.json.text.diff.right.title"));

        DiffManager.getInstance().showDiff(project, simpleDiffRequest, DiffDialogHints.NON_MODAL);
    }
}
