package cn.memoryzy.json.actions;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.JsonUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/6/21
 */
public class JsonFormatAction extends AnAction {

    public JsonFormatAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.format.text"));
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = PlatformUtil.getEditor(e);
        Document document = editor.getDocument();
        String content = document.getText();
        String jsonStr = (JsonUtil.isJsonStr(content)) ? content : JsonUtil.extractJsonStr(content);
        String formattedJson = JsonUtil.formatJson(jsonStr);
        // 获取当前文档内的psiFile
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);

        // todo 编辑器文本不属于Json，但是选中文本属于Json时，可以
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.setText(formattedJson);
            // 格式化
            if (Objects.nonNull(psiFile)) {
                CodeStyleManager.getInstance(project).reformatText(psiFile, 0, document.getTextLength());
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        String content = PlatformUtil.getEditorContent(e);
        String jsonStr = (JsonUtil.isJsonStr(content)) ? content : JsonUtil.extractJsonStr(content);
        e.getPresentation().setEnabledAndVisible(StrUtil.isNotBlank(jsonStr));
    }
}
