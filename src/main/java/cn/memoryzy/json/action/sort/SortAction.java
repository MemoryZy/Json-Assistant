package cn.memoryzy.json.action.sort;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.sort.SortStrategy;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.GlobalTextConverter;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.processor.json.JsonConversionProcessor;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.util.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2025/9/2
 */
public abstract class SortAction extends DumbAwareAction implements UpdateInBackground {

    public SortAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Project project = getEventProject(e);
        Editor editor = PlatformUtil.getEditor(dataContext);
        EditorData editorData = GlobalTextConverter.resolveEditor(editor);

        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        // 获取解析器集合
        JsonConversionProcessor[] processors = GlobalTextConversionProcessorContext.getOriginalAllJsonProcessors(editorData);
        // 解析JSON
        String json = GlobalJsonConverter.parseJson(context, processors);
        // 是否为Json格式
        boolean isJson = JsonUtil.isJson(json);
        // 排序
        JsonWrapper wrapper = JsonSorter.sortJson(json, isJson, getStrategy());
        // 设置提示文本
        GlobalJsonConverter.setHintMessage(processors, JsonAssistantBundle.messageOnSystem("hint.selection.sort"), JsonAssistantBundle.messageOnSystem("hint.global.sort"));

        String jsonString;
        if (JsonUtil.isFormattedJson(json)) {
            jsonString = isJson ? JsonUtil.formatJson(wrapper) : Json5Util.formatJson5(wrapper);
        } else {
            jsonString = isJson ? JsonUtil.compressJson(wrapper) : Json5Util.compressJson5(wrapper);
        }

        if (StrUtil.isNotBlank(jsonString)) {
            AbstractGlobalTextConversionProcessor processor = context.getProcessor();
            boolean hasSelection = processor.getEditorData().getSelectionData().isHasSelection();
            String[] allowedFileTypeQualifiedNames = processor.getFileTypeData().getAllowedFileTypeQualifiedNames();
            boolean canWrite = TextTransformUtil.canWriteToDocument(dataContext, editor, hasSelection, allowedFileTypeQualifiedNames);
            TextTransformUtil.applyProcessedTextToDocument(project, editor, jsonString, processor, canWrite, null);
        }
    }

    protected abstract SortStrategy getStrategy();

}
