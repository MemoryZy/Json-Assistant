package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class OtherFormatsToJsonAction extends DumbAwareAction {

    // todo 在update的时候检测是哪种格式，然后在线更改action text和desc

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

    }

    @Override
    public void update(@NotNull AnActionEvent e) {

    }
}
