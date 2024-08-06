package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.UpdateHolder;
import cn.memoryzy.json.group.JsonProcessingPopupGroup;
import cn.memoryzy.json.model.formats.JsonFormatHandleModel;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/7/2
 */
public class JsonAssistantAction extends DumbAwareAction implements UpdateInBackground {

    public JsonAssistantAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.processing.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.processing.description"));
        presentation.setIcon(JsonAssistantIcons.BOX);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new JsonProcessingPopupGroup(true).actionPerformed(e);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean isHasJsonStr = false;
        Editor editor = PlatformUtil.getEditor(e);
        Presentation presentation = e.getPresentation();
        presentation.setVisible(false);
        if (editor != null) {
            isHasJsonStr = isOrHasJsonStr(editor);
        }

        presentation.setEnabled(isHasJsonStr);
        UpdateHolder.supplementaryState(isHasJsonStr);
    }

    public static boolean isOrHasJsonStr(Editor editor) {
        return JsonFormatHandleModel.of(editor).isJsonStr();
    }

}
