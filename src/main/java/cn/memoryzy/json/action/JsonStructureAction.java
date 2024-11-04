package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.ui.dialog.JsonStructureDialog;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
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
        presentation.setIcon(JsonAssistantIcons.STRUCTURE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        JsonStructureDialog.show(GlobalJsonConverter.parseJson(
                dataContext, PlatformUtil.getEditor(dataContext), true));
    }

}
