package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.group.JsonProcessingPopupGroup;
import cn.memoryzy.json.model.formats.JsonFormatHandleModel;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/2
 */
public class JsonAssistantAction extends DumbAwareAction {

    public JsonAssistantAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.processing.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.processing.description"));
        presentation.setIcon(PlatformUtil.isNewUi() ? JsonAssistantIcons.ExpUi.NEW_BOX : JsonAssistantIcons.BOX);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new JsonProcessingPopupGroup().actionPerformed(e);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(isOrHasJsonStr(e) || isOrHasMatchTypeStr(e));
    }

    public static boolean isOrHasJsonStr(@NotNull AnActionEvent e) {
        Editor editor = PlatformUtil.getEditor(e);
        return editor != null && JsonFormatHandleModel.of(editor).isJsonStr();
    }

    public static boolean isOrHasMatchTypeStr(@NotNull AnActionEvent e) {
        Editor editor = PlatformUtil.getEditor(e);
        return editor != null && Objects.nonNull(JsonAssistantUtil.matchFormats(editor));
    }

}
