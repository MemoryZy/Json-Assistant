package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.model.strategy.GlobalTextConverter;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.BulkKvLinesUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * 将 Bulk KV Lines（批量键值行格式）文本转换为 JSON。
 * <p>
 * 支持嵌套对象（{@code a.b}）、数组（{@code list[0]}）与嵌套数组对象（{@code coll[0].actions[0].label}）；
 * 行首带 {@code //} 的禁用行按该格式语义跳过。具体规则见 {@link BulkKvLinesUtil}。
 * <p>
 * 该 action 与 {@link cn.memoryzy.json.action.OtherFormatsToJsonAction} 的自动识别互不干扰：
 * Bulk 文本本身也符合 YAML 的语法（{@code key:} 是合法的 YAML 键），若放进自动识别链路，
 * 会被 YAML 处理器抢先解析并丢掉类型信息，因此这里采用显式触发的方式。
 *
 * @author Memory
 * @since 2026/11/19
 */
public class BulkKvLinesToJsonAction extends DumbAwareAction implements UpdateInBackground {

    public BulkKvLinesToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.bulk.kv.lines.to.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.bulk.kv.lines.to.json.description"));
        presentation.setIcon(JsonAssistantIcons.JSON);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        String content = getContent(event);
        // 内容不符合 Bulk 格式时直接返回，不做任何提示
        if (!BulkKvLinesUtil.canBulkBeConvertedToJson(content)) {
            return;
        }

        String json = BulkKvLinesUtil.bulkToJson(content);
        if (json == null) {
            return;
        }

        TextTransformUtil.applyTextWhenNotWritable(getEventProject(event), json, FileTypeHolder.JSON, "JSON");
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        // 只有当前文本能按 Bulk 解析时才展示该 action，避免在普通文本里出现无意义的菜单项
        event.getPresentation().setEnabledAndVisible(BulkKvLinesUtil.canBulkBeConvertedToJson(getContent(event)));
    }

    /**
     * 取出用于转换的文本：选中文本优先，否则使用整篇文档
     *
     * @param event action 事件
     * @return 文本；编辑器不可用时返回 null
     */
    private String getContent(@NotNull AnActionEvent event) {
        Editor editor = PlatformUtil.getEditor(event.getDataContext());
        if (editor == null) {
            return null;
        }

        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (editorData == null) {
            return null;
        }

        return editorData.getSelectionData().isHasSelection()
                ? editorData.getDocTextData().getSelectedText()
                : editorData.getDocTextData().getDocumentText();
    }

}
