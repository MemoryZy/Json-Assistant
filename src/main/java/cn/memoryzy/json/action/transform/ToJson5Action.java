package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/11/4
 */
public class ToJson5Action extends DumbAwareAction implements UpdateInBackground {

    public ToJson5Action() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.to.json5.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.to.json5.description"));
        presentation.setIcon(AllIcons.FileTypes.JsonSchema);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        GlobalJsonConverter.convertBetweenJsonAndJson5(
                dataContext,
                PlatformUtil.getEditor(dataContext),
                Json5Util::jsonToJson5,
                JsonAssistantBundle.messageOnSystem("hint.selection.json.to.json5.text"),
                JsonAssistantBundle.messageOnSystem("hint.global.json.to.json5.text"));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 确保当前是JSON
        DataContext dataContext = e.getDataContext();
        e.getPresentation().setEnabledAndVisible(
                GlobalJsonConverter.validateEditorJson(
                        getEventProject(e),
                        PlatformUtil.getEditor(dataContext),
                        dataContext));
    }
}
