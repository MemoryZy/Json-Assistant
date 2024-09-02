package cn.memoryzy.json.action;

import cn.memoryzy.json.action.group.JsonAssistantPopupGroup;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.formats.JsonFormatHandleModel;
import cn.memoryzy.json.util.PlatformUtil;
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
        presentation.setText(JsonAssistantBundle.message("action.json.assistant.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.assistant.description"));
        presentation.setIcon(JsonAssistantIcons.BOX);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new JsonAssistantPopupGroup(true).actionPerformed(e);
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
    }

    public static boolean isOrHasJsonStr(Editor editor) {
        return JsonFormatHandleModel.of(editor).isJsonStr();
    }

}
