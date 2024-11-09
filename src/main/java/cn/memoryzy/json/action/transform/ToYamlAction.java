package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import cn.memoryzy.json.util.YamlUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/9/19
 */
public class ToYamlAction extends DumbAwareAction {

    private static final Logger LOG = Logger.getInstance(ToYamlAction.class);

    public ToYamlAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.to.yaml.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.to.yaml.description"));
        presentation.setIcon(AllIcons.FileTypes.Yaml);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // 现在默认是新开窗口展示转换后的文本，所以不需要提示了
        // JsonAssistantBundle.messageOnSystem("hint.selection.json.to.yaml.text")
        // JsonAssistantBundle.messageOnSystem("hint.global.json.to.yaml.text")
        DataContext dataContext = event.getDataContext();

        String yamlStr;
        try {
            GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
            String json = GlobalJsonConverter.parseJson(dataContext, context, PlatformUtil.getEditor(dataContext));
            // 处理不同的 Json 类型
            yamlStr = YamlUtil.toYaml(json, GlobalJsonConverter.isValidJson(context.getProcessor()));
        } catch (Exception ex) {
            LOG.error("Yaml conversion failure", ex);
            return;
        }

        TextTransformUtil.applyTextWhenNotWritable(getEventProject(event), yamlStr, FileTypeHolder.YAML);
    }
}
