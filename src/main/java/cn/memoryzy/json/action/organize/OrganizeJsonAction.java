package cn.memoryzy.json.action.organize;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.GlobalTextConverter;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.processor.json.JsonConversionProcessor;
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
import java.util.Objects;

/**
 * 「整理 Json…」子菜单中各个 action 的公共父类
 * <p>
 * 这一组 action 共用同一套处理流程，只有「去除哪一类无意义数据」不同，因此把流程收敛到本类中：
 * <ol>
 *     <li>从数据上下文取出编辑器，并解析出 {@link EditorData}（用于区分选中文本与全文）</li>
 *     <li>用 JSON / JSON5 解析器把当前内容解析成 JSON 文本（选中的是 JSON 就用选中文本，否则用全文）</li>
 *     <li>交给 {@link JsonValuePruner} 按指定 {@link cn.memoryzy.json.util.JsonValuePruner.Mode 模式} 清理</li>
 *     <li>没有可去除的数据时只弹提示，不重写文本；有去除时按原排版风格写回文档</li>
 * </ol>
 * 子类只需要在构造方法里说明「自己的文案、清理模式、文案中的类别名」，无需重复实现流程。
 *
 * @author Memory
 * @since 2026/11/17
 */
public abstract class OrganizeJsonAction extends DumbAwareAction implements UpdateInBackground {

    /**
     * 清理模式（决定本次去除哪几类无意义数据）
     */
    private final JsonValuePruner.Mode pruneMode;

    /**
     * 文案中的「类别名」的键名
     * <p>
     * 用于拼出提示文本，例如「已去除 5 项（null 值）」中的「null 值」；
     * 这样各个子 action 的提示文案可以共用同一套模板，不必为每一类单独维护三份提示文案。
     */
    private final String categoryKey;

    public OrganizeJsonAction(@Nullable String text,
                              @Nullable String description,
                              @NotNull JsonValuePruner.Mode pruneMode,
                              @NotNull String categoryKey) {
        super(text, description, null);
        this.pruneMode = pruneMode;
        this.categoryKey = categoryKey;
        // 允许在对话框（modal context）中使用，与项目内其他 action 保持一致
        setEnabledInModalContext(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = getEventProject(event);

        // 1、解析编辑器（没有编辑器时直接返回，例如在欢迎页、设置页中触发）
        Editor editor = PlatformUtil.getEditor(dataContext);
        if (Objects.isNull(editor)) {
            return;
        }

        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (Objects.isNull(editorData)) {
            return;
        }

        // 2、准备解析器集合（标准 JSON、JSON5），并把当前内容解析成 JSON 文本
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        JsonConversionProcessor[] processors = GlobalTextConversionProcessorContext.getOriginalAllJsonProcessors(editorData);
        String json = GlobalJsonConverter.parseJson(context, processors);
        if (StrUtil.isBlank(json)) {
            return;
        }

        // 3、按当前 action 对应的模式去除无意义数据
        JsonValuePruner.PruneResult result = JsonValuePruner.prune(json, pruneMode);

        // 4、解析失败时静默返回；没有可去除的数据时只给提示，不重写文本（避免无意义地改动文档）
        // 类别名（例如「null 值」）用于拼出下面两条提示文案
        String category = JsonAssistantBundle.messageOnSystem(categoryKey);
        if (Objects.isNull(result) || !result.isChanged()) {
            HintUtil.showInformationHint(editor, JsonAssistantBundle.messageOnSystem("hint.none.organize.json", category));
            return;
        }

        // 5、设置处理成功后的提示文本（选中文本 / 全文），并把去除的数量反馈给用户
        GlobalJsonConverter.setHintMessage(processors,
                JsonAssistantBundle.messageOnSystem("hint.selection.organize.json", result.getRemovedCount(), category),
                JsonAssistantBundle.messageOnSystem("hint.global.organize.json", result.getRemovedCount(), category));

        AbstractGlobalTextConversionProcessor processor = context.getProcessor();
        boolean hasSelection = processor.getEditorData().getSelectionData().isHasSelection();
        String[] allowedFileTypeQualifiedNames = processor.getFileTypeData().getAllowedFileTypeQualifiedNames();
        // 判断当前文档是否允许写入（不可写入时，会在工具窗口中新开选项页展示处理结果）
        boolean canWrite = TextTransformUtil.canWriteToDocument(dataContext, editor, hasSelection, allowedFileTypeQualifiedNames);

        // 6、将整理后的文本应用至文档
        TextTransformUtil.applyProcessedTextToDocument(project, editor, result.getText(), processor, canWrite, getResultTabName());
    }

    /**
     * 结果页的标签名
     * <p>
     * 仅在「当前文件不可写入」时才会用到（此时会在工具窗口中新建一个选项页展示结果）。
     * 不同模式用不同标签，便于同时打开多个结果页时区分。
     *
     * @return 结果页标签名
     */
    private String getResultTabName() {
        switch (pruneMode) {
            case NULL:
                return "Tidy Null";
            case EMPTY:
                return "Tidy Empty";
            case ZERO:
                return "Tidy Zero";
            case ALL:
            default:
                return "Tidy All";
        }
    }
}
