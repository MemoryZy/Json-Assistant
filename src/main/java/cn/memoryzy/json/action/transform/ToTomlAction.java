package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import cn.memoryzy.json.util.TomlUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/9/25
 */
public class ToTomlAction extends DumbAwareAction implements UpdateInBackground {

    private static final Logger LOG = Logger.getInstance(ToTomlAction.class);

    public ToTomlAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.to.toml.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.to.toml.description"));
        presentation.setIcon(JsonAssistantIcons.FileTypes.TOML);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // 现在默认是新开窗口展示转换后的文本，所以不需要提示了
        // JsonAssistantBundle.messageOnSystem("hint.selection.json.to.toml.text");
        // JsonAssistantBundle.messageOnSystem("hint.global.json.to.toml.text");
        DataContext dataContext = event.getDataContext();

        String tomlStr;
        try {
            GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
            String json = GlobalJsonConverter.parseJson(dataContext, context, PlatformUtil.getEditor(dataContext));
            // 处理不同的 Json 类型
            tomlStr = TomlUtil.toToml(json, GlobalJsonConverter.isValidJson(context.getProcessor()));
        } catch (Exception ex) {
            LOG.error("Toml conversion failure", ex);
            return;
        }

        TextTransformUtil.applyTextWhenNotWritable(getEventProject(event), tomlStr, FileTypeHolder.TOML);
    }


    @Override
    public void update(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String json = GlobalJsonConverter.parseJson(dataContext, context, PlatformUtil.getEditor(dataContext));
        event.getPresentation().setEnabled(TomlUtil.canConvertToToml(json, GlobalJsonConverter.isValidJson(context.getProcessor())));
    }
}
