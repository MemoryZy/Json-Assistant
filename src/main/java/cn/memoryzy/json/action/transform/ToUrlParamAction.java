package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/11/8
 */
public class ToUrlParamAction extends DumbAwareAction implements UpdateInBackground {

    public ToUrlParamAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.to.url.param.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.to.url.param.description"));
        presentation.setIcon(JsonAssistantIcons.FileTypes.URL);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // JsonMap中跳过Map、List、null、长文本String
        DataContext dataContext = event.getDataContext();
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String json = GlobalJsonConverter.parseJson(dataContext, context, PlatformUtil.getEditor(dataContext));
        String urlParamStr = TextTransformUtil.jsonToUrlParams(json, GlobalJsonConverter.isValidJson(context.getProcessor()));
        TextTransformUtil.copyToClipboardAndShowNotification(getEventProject(event), urlParamStr);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(ToTomlAction.canConvertToTomlOrUrlParam(event.getDataContext()));
    }
}
