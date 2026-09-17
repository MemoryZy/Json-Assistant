package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.util.CsvUtil;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * 将 JSON 数组对象转换为 CSV 格式。
 *
 * @author Memory
 * @since 2026/09/16
 */
public class ToCsvAction extends DumbAwareAction implements UpdateInBackground {

    public ToCsvAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.to.csv.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.to.csv.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String json = GlobalJsonConverter.parseJson(context, PlatformUtil.getEditor(dataContext));
        if (json == null) return;

        String csv = CsvUtil.jsonToCsv(json, JsonUtil.isJson(json));
        if (csv == null) return;

        TextTransformUtil.applyTextWhenNotWritable(getEventProject(event), csv, FileTypeHolder.CSV, "CSV");
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String json = GlobalJsonConverter.parseJson(context, PlatformUtil.getEditor(dataContext));
        event.getPresentation().setEnabled(json != null && CsvUtil.isArrayOfObjects(json));
    }

}