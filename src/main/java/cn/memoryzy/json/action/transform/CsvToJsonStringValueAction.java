package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.model.strategy.GlobalTextConverter;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.util.CsvUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * 将 CSV 文本转换为 JSON 数组（不进行类型推断，所有值按字符串处理）。
 * <br/>
 * <br/>
 * <p style="color:red">因为 OtherFormatsToJsonAction 类提供了一个类型推断功能的 CSV 转 JSON 的Action，他同时只能展示一个处理器，</p>
 * <p style="color:red">所以在此新增一个 Action，用于处理 CSV 转 JSON（默认使用 String 做值，不做类型推断）的功能</p>
 * @author Memory
 * @since 2026/09/16
 */
public class CsvToJsonStringValueAction extends DumbAwareAction implements UpdateInBackground {

    public CsvToJsonStringValueAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.csv.to.json.string.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.csv.to.json.string.description"));
        presentation.setIcon(AllIcons.FileTypes.Text);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = PlatformUtil.getEditor(event.getDataContext());
        if (editor == null) return;

        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (editorData == null) return;

        String csv;
        if (editorData.getSelectionData().isHasSelection()) {
            csv = editorData.getDocTextData().getSelectedText();
        } else {
            csv = editorData.getDocTextData().getDocumentText();
        }

        if (!CsvUtil.canCsvBeConvertedToJson(csv)) return;

        String json = CsvUtil.csvToJson(csv, false);
        if (json == null) return;

        TextTransformUtil.applyTextWhenNotWritable(getEventProject(event), json, FileTypeHolder.JSON, "JSON");
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Editor editor = PlatformUtil.getEditor(event.getDataContext());
        boolean enabled = false;
        if (editor != null) {
            EditorData editorData = GlobalTextConverter.resolveEditor(editor);
            if (editorData != null) {
                String csv = editorData.getSelectionData().isHasSelection()
                        ? editorData.getDocTextData().getSelectedText()
                        : editorData.getDocTextData().getDocumentText();
                enabled = CsvUtil.canCsvBeConvertedToJson(csv);
            }
        }
        event.getPresentation().setEnabledAndVisible(enabled);
    }

}