package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/11/5
 */
public class JsonEscapeAction extends DumbAwareAction {

    public JsonEscapeAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.escape.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.escape.description"));
        presentation.setIcon(JsonAssistantIcons.CONVERSION);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();

        String escapeJson = StringEscapeUtils.escapeJson(
                GlobalJsonConverter.parseJson(dataContext, PlatformUtil.getEditor(dataContext)));

        TextTransformUtil.copyToClipboardAndShowNotification(getEventProject(event), escapeJson);

        // TextTransformUtil.applyTextWhenNotWritable(getEventProject(event), escapeJson, PlainTextFileType.INSTANCE);
    }

}
