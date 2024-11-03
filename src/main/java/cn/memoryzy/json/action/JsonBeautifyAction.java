package cn.memoryzy.json.action;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.formats.JsonProcessor;
import cn.memoryzy.json.model.strategy.formats.context.ConversionProcessorContext;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/6/21
 */
public class JsonBeautifyAction extends DumbAwareAction {

    private static final Logger LOG = Logger.getInstance(JsonBeautifyAction.class);

    public JsonBeautifyAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.beautify.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.beautify.description"));
        presentation.setIcon(JsonAssistantIcons.DIZZY_STAR);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = PlatformUtil.getEditor(e);
        beautifyJson(e, editor);
    }

    public static void beautifyJson(AnActionEvent e, Editor editor) {
        ConversionProcessorContext context = new ConversionProcessorContext();
        JsonProcessor jsonProcessor = new JsonProcessor(e.getDataContext(), TextTransformUtil.resolveEditor(editor), true);
        setBeautifyMessage(jsonProcessor);

        String processedText = ConversionProcessorContext.applyProcessors(context, new JsonProcessor[]{jsonProcessor});
        if (StrUtil.isNotBlank(processedText)) {
            boolean hasSelection = jsonProcessor.getEditorInfo().getSelectionInfo().isHasSelection();
            String[] allowedFileTypeQualifiedNames = jsonProcessor.getFileTypeInfo().getAllowedFileTypeQualifiedNames();
            boolean canWrite = TextTransformUtil.canWriteToDocument(e.getDataContext(), editor.getDocument(), hasSelection, allowedFileTypeQualifiedNames);
            // 因为在处理器的后置处理程序提供了格式化，所以无需处理
            TextTransformUtil.applyProcessedTextToDocument(getEventProject(e), editor, processedText, jsonProcessor, canWrite);
        }
    }

    private static void setBeautifyMessage(JsonProcessor jsonProcessor) {
        jsonProcessor.getMessageInfo()
                .setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.json.beautify.text"))
                .setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.json.beautify.text"));
    }

}
