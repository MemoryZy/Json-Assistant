package cn.memoryzy.json.action.transform;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.util.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * 将 JSON/JSON5 转换为 TypeScript 类型声明
 *
 * @author Memory
 * @since 2026/09/02
 */
public class ToTypeScriptAction extends DumbAwareAction implements UpdateInBackground {

    private static final Logger LOG = Logger.getInstance(ToTypeScriptAction.class);

    /**
     * 根类型默认名称
     */
    private static final String ROOT_TYPE_NAME = "RootObject";

    public ToTypeScriptAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.to.typescript.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.to.typescript.description"));
        presentation.setIcon(JsonAssistantIcons.FileTypes.TYPESCRIPT);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();

        String tsStr;
        try {
            GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
            String json = GlobalJsonConverter.parseJson(context, PlatformUtil.getEditor(dataContext));
            if (StrUtil.isBlank(json)) {
                return;
            }

            JsonWrapper wrapper;
            boolean includeComment;
            if (JsonUtil.isJson(json)) {
                wrapper = JsonUtil.parse(json);
                includeComment = false;
            } else if (Json5Util.isJson5(json)) {
                // JSON5 支持注释，解析时保留注释
                wrapper = Json5Util.parseWithComment(json);
                includeComment = true;
            } else {
                return;
            }

            tsStr = TypeScriptUtil.jsonToTypeScript(wrapper, ROOT_TYPE_NAME, includeComment);
        } catch (Exception ex) {
            LOG.error("[Json Assistant] TypeScript conversion failure", ex);
            return;
        }

        TextTransformUtil.applyTextWhenNotWritable(getEventProject(event), tsStr, FileTypeHolder.TYPESCRIPT, "TypeScript");
    }
}
