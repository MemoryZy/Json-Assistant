package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.GlobalTextConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.processor.json.JsonConversionProcessor;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/11/5
 */
public class JsonEscapeAction extends DumbAwareAction implements UpdateInBackground {

    public JsonEscapeAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.escape.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.escape.description"));
        presentation.setIcon(JsonAssistantIcons.CONVERSION);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Editor editor = PlatformUtil.getEditor(dataContext);
        EditorData editorData = GlobalTextConverter.resolveEditor(editor);

        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        // 验证
        if (Objects.isNull(editorData)) return;
        // 获取解析器集合
        JsonConversionProcessor[] processors = GlobalTextConversionProcessorContext.getOriginalAllJsonProcessors(editorData);
        // 解析JSON
        String json = GlobalJsonConverter.parseJson(context, processors);
        // 转义
        String escapeJson = StringEscapeUtils.escapeJson(json);
        // 不对换行符进行转义，保留原本格式
        String recoverEscapeJson = escapeJson.replace("\\n", "\n");
        TextTransformUtil.copyToClipboardAndShowNotification(getEventProject(event), escapeJson);
        TextTransformUtil.applyTextWhenNotWritable(getEventProject(event), recoverEscapeJson, PlainTextFileType.INSTANCE, "Escape");
    }

}
