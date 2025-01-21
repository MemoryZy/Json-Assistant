package cn.memoryzy.json.action;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.editor.DiffVirtualFile;
import com.intellij.diff.editor.SimpleDiffVirtualFile;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/14
 */
public class JsonTextDiffAction extends DumbAwareAction {

    public static final Key<SimpleDiffRequest> DIFF_REQUEST_KEY = Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".DiffRequest");

    public JsonTextDiffAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.text.diff.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.text.diff.description"));
        presentation.setIcon(JsonAssistantIcons.DIFF);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        DataContext dataContext = event.getDataContext();

        String leftJsonStr = StrUtil.trim(GlobalJsonConverter.parseJson(PlatformUtil.getEditor(dataContext), true));

        FileType fileType = FileTypeHolder.JSON5;
        DiffContentFactory diffContentFactory = DiffContentFactory.getInstance();
        DocumentContent leftDocumentContent = diffContentFactory.createEditable(project, leftJsonStr, fileType);
        DocumentContent rightDocumentContent = diffContentFactory.createEditable(project, "", fileType);

        SimpleDiffRequest simpleDiffRequest = new SimpleDiffRequest(
                JsonAssistantBundle.messageOnSystem("dialog.json.text.diff.main.title"),
                leftDocumentContent,
                rightDocumentContent,
                JsonAssistantBundle.messageOnSystem("dialog.json.text.diff.left.title"),
                JsonAssistantBundle.messageOnSystem("dialog.json.text.diff.right.title"));

        DiffVirtualFile file = new SimpleDiffVirtualFile(simpleDiffRequest);
        file.putUserData(DIFF_REQUEST_KEY, simpleDiffRequest);
        FileEditorManager manager = FileEditorManager.getInstance(Objects.requireNonNull(project));
        JsonAssistantUtil.invokeMethod(manager, "openFileInNewWindow", file);
    }
}
