package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.model.formats.BaseFormatModel;
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
 * @since 2024/8/3
 */
public class OtherFormatsToJsonAction extends DumbAwareAction {

    public OtherFormatsToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.other.formats.to.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.other.formats.to.json.description"));
        presentation.setIcon(JsonAssistantIcons.CONVERSION);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = PlatformUtil.getEditor(e);
        BaseFormatModel model = JsonAssistantUtil.matchFormats(editor);
        if (Objects.nonNull(model)) {
            JsonAssistantUtil.writeOrCopyJsonOnEditor(e.getProject(), editor, editor.getDocument(), model.convertToJson(), model, true);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = false;
        Presentation presentation = e.getPresentation();
        BaseFormatModel model = JsonAssistantUtil.matchFormats(PlatformUtil.getEditor(e));
        if (Objects.nonNull(model)) {
            enabled = true;
            presentation.setText(model.getActionName());
            presentation.setDescription(model.getActionDescription());
        }

        presentation.setEnabledAndVisible(enabled);
    }
}
