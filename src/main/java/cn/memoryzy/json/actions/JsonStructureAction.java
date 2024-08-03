package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.model.JsonEditorInfoModel;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class JsonStructureAction extends DumbAwareAction {

    public JsonStructureAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.structure.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.structure.description"));
        presentation.setIcon(JsonAssistantIcons.SMALL_STRUCTURE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = PlatformUtil.getEditor(event);
        JsonEditorInfoModel info = new JsonEditorInfoModel(editor);
        JsonAssistantUtil.showJsonStructureDialog(info.jsonContent);
    }

}
