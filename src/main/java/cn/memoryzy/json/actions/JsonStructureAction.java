package cn.memoryzy.json.actions;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.actions.child.JsonStructureOnTwTitleAction;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class JsonStructureAction extends AnAction {

    public JsonStructureAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.structure.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        String text = StrUtil.trim(PlatformUtil.getEditorContent(event));
        // todo 编辑器文本不属于Json，但是选中文本属于Json时，可以
        JsonStructureOnTwTitleAction.structuring(text, project);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        String text = StrUtil.trim(PlatformUtil.getEditorContent(e));
        e.getPresentation().setEnabledAndVisible(JsonStructureOnTwTitleAction.structuringUpdate(text));
    }
}
