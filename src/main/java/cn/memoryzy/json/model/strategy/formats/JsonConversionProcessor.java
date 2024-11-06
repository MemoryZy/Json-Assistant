package cn.memoryzy.json.model.strategy.formats;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.FileTypes;
import cn.memoryzy.json.model.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;

import java.util.Objects;

/**
 * 不参与策略处理
 *
 * @author Memory
 * @since 2024/11/3
 */
public class JsonConversionProcessor extends AbstractGlobalTextConversionProcessor {

    private DataContext dataContext;

    public JsonConversionProcessor(EditorData editorData, boolean needBeautify) {
        super(editorData, needBeautify);
    }

    public JsonConversionProcessor(DataContext dataContext, EditorData editorData, boolean needBeautify) {
        super(editorData, needBeautify);
        this.dataContext = dataContext;
        String[] fileTypes = {FileTypes.JSON.getFileTypeQualifiedName(), FileTypes.JSON5.getFileTypeQualifiedName()};
        getFileTypeData().setAllowedFileTypeQualifiedNames(fileTypes);
    }

    @Override
    public boolean canConvert(String text) {
        return JsonUtil.isJson(text) || StrUtil.isNotBlank(extractJson(text));
    }

    @Override
    public String convertToJson() {
        String contentStr = getContent();
        if (JsonUtil.isJson(contentStr)) {
            return contentStr;
        }

        contentStr = extractJson(contentStr);
        return StrUtil.isNotBlank(contentStr) ? contentStr : null;
    }



    // -------------------------- Private Method -------------------------- //

    /**
     * 提取 JSON 文本
     *
     * @param text 文本
     * @return 提取出的文本
     */
    private String extractJson(String text) {
        if (Objects.isNull(dataContext)) {
            return null;
        }

        // 是否使用全局文本做匹配
        String documentText = editorData.getDocTextData().getDocumentText();
        if (Objects.equals(documentText, text)) {
            // 效率优化（防止全部文本过多）
            Project project = CommonDataKeys.PROJECT.getData(dataContext);
            Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
            // 如果未找到编辑器，则不开启提取 JSON 功能
            if (Objects.isNull(editor)) {
                return null;
            }

            Document document = editor.getDocument();
            int lineCount = document.getLineCount();
            FileType fileType = PlatformUtil.getDocumentFileType(project, document);

            // 全部文本未超过 300 行，或者文件类型是指定类型，则解析提取 JSON
            if (lineCount < 300 || JsonAssistantUtil.isJsonFileType(fileType)) {
                return JsonUtil.extractJson(documentText);
            }
        }

        return JsonUtil.extractJson(text);
    }

}
