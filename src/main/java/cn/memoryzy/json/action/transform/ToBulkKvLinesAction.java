package cn.memoryzy.json.action.transform;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.BulkKvLinesUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * 将 JSON 转换为 Bulk KV Lines（批量键值行格式）。
 * <p>
 * 转换结果在多行文本中逐行展示 {@code 键:值}，嵌套对象用点号、数组用方括号下标表达，
 * 可以直接粘贴到支持该格式的编辑面板中。具体规则见 {@link BulkKvLinesUtil}。
 *
 * @author Memory
 * @since 2026/11/19
 */
public class ToBulkKvLinesAction extends DumbAwareAction implements UpdateInBackground {

    public ToBulkKvLinesAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.to.bulk.kv.lines.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.to.bulk.kv.lines.description"));
        presentation.setIcon(JsonAssistantIcons.FileTypes.KV);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String json = GlobalJsonConverter.parseJson(context, PlatformUtil.getEditor(dataContext));

        // JSON / JSON5 分别按各自的解析器处理
        String bulk = BulkKvLinesUtil.jsonToBulk(json, JsonUtil.isJson(json));
        // 空对象 / 空数组没有可输出的键值对，此时不做任何操作，避免弹出空白页
        if (StrUtil.isBlank(bulk)) {
            return;
        }

        TextTransformUtil.applyTextWhenNotWritable(getEventProject(event), bulk, FileTypeHolder.PLAIN_TEXT, "Bulk KV Lines");
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String json = GlobalJsonConverter.parseJson(context, PlatformUtil.getEditor(dataContext));
        // 只要当前内容是 JSON / JSON5 就可以转换（根节点是数组时同样支持）
        event.getPresentation().setEnabled(json != null && (JsonUtil.isJson(json) || Json5Util.isJson5(json)));
    }

}
