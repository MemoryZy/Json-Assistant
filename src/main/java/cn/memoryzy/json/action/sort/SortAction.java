package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.model.sort.SortStrategy;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.GlobalTextConverter;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.processor.json.JsonConversionProcessor;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
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
        SortStrategy strategy = getStrategy();
        DataContext dataContext = e.getDataContext();
        Editor editor = PlatformUtil.getEditor(dataContext);
        EditorData editorData = GlobalTextConverter.resolveEditor(editor);

        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        // 获取解析器集合
        JsonConversionProcessor[] processors = GlobalTextConversionProcessorContext.getOriginalAllJsonProcessors(editorData);
        // 解析JSON
        String json = GlobalJsonConverter.parseJson(context, processors);
        // 获取解析成功的处理器
        AbstractGlobalTextConversionProcessor processor = context.getProcessor();
        // 是否为 JSON 格式
        boolean isJson = JsonUtil.isJson(json);

        // 解析
        JsonWrapper wrapper = isJson ? JsonUtil.parse(json) : Json5Util.parse(json);
        if (wrapper == null) {
            return;
        }

        JsonUtil.isFormattedJson(json);


    }

    protected abstract SortStrategy getStrategy();

}
