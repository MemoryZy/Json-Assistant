package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.model.strategy.GlobalTextConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.processor.BulkKvLinesConversionProcessor;
import cn.memoryzy.json.util.BulkKvLinesUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * 将 Bulk KV Lines（批量键值行格式）文本转换为 JSON（不进行类型推断，所有值按字符串处理）。
 * <br/>
 * <br/>
 * <p>因为 OtherFormatsToJsonAction 类提供了一个类型推断功能的 Bulk KV Lines 转 JSON 的处理链，
 * 而它同时只会展示一个处理器，所以在此新增一个 Action，用于处理「Bulk KV Lines 转 JSON
 * （值默认使用 String，不做类型推断）」的功能，行为与 {@link CsvToJsonStringValueAction} 保持一致：</p>
 * <ul>
 *     <li>转换结果直接在新标签页中展示，不覆盖当前文档；</li>
 *     <li>与「其它格式转 JSON」共用同一条识别链，只有当识别链最终选中的是 Bulk KV Lines 处理器时，
 *     本 action 才会和它一起展示（否则两者都不出现）。</li>
 * </ul>
 * <p>与该格式相关的解析规则（嵌套对象、数组下标、{@code //} 禁用行等）全部由 {@link BulkKvLinesUtil} 负责。</p>
 *
 * @author Memory
 * @since 2026/11/21
 */
public class BulkKvLinesToJsonStringValueAction extends DumbAwareAction implements UpdateInBackground {

    public BulkKvLinesToJsonStringValueAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.bulk.kv.lines.to.json.string.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.bulk.kv.lines.to.json.string.description"));
        presentation.setIcon(JsonAssistantIcons.FileTypes.KV);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = PlatformUtil.getEditor(event.getDataContext());
        if (editor == null) return;

        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (editorData == null) return;

        // 选中文本优先，否则使用整篇文档
        String bulk = editorData.getSelectionData().isHasSelection()
                ? editorData.getDocTextData().getSelectedText()
                : editorData.getDocTextData().getDocumentText();

        // 不符合 Bulk 格式时直接返回，不做任何提示
        if (!BulkKvLinesUtil.canBulkBeConvertedToJson(bulk)) return;

        // 第二个参数为 false：关闭类型推断，所有值都保持字符串
        String json = BulkKvLinesUtil.bulkToJson(bulk, false);
        if (json == null) return;

        TextTransformUtil.applyTextWhenNotWritable(getEventProject(event), json, FileTypeHolder.JSON, "JSON");
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        boolean enabled = false;
        Editor editor = PlatformUtil.getEditor(event.getDataContext());
        if (editor != null) {
            // 复用「其它格式转 JSON」的同一套识别链：识别链按顺序逐个尝试处理器，同一时刻只会有一个处理器命中。
            // 这里只在命中的是 Bulk KV Lines 处理器时才展示本 action，于是
            // 「其它格式转 JSON」（此时展示为 Bulk KV Lines 推断类型的那一项）与本 action 要么一起出现、要么都不出现，
            // 避免 YAML 等文本因 Bulk 判断较宽松而同时冒出两个 Bulk 菜单项。
            GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
            if (GlobalTextConverter.validateEditorText(context, editor)) {
                enabled = context.getProcessor() instanceof BulkKvLinesConversionProcessor;
            }
        }
        event.getPresentation().setEnabledAndVisible(enabled);
    }

}
