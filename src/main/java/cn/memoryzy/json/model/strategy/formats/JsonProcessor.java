package cn.memoryzy.json.model.strategy.formats;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.FileTypeEnum;
import cn.memoryzy.json.model.formats.EditorInfo;
import cn.memoryzy.json.model.strategy.formats.context.AbstractConversionProcessor;
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
 * 不参与策略处理，只存储一些信息
 *
 * @author Memory
 * @since 2024/11/3
 */
public class JsonProcessor extends AbstractConversionProcessor {

    private final DataContext dataContext;

    public JsonProcessor(DataContext dataContext, EditorInfo editorInfo, boolean needsFormatting) {
        super(editorInfo, needsFormatting);
        this.dataContext = dataContext;
        String[] fileTypes = {FileTypeEnum.JSON.getFileTypeQualifiedName(), FileTypeEnum.JSON5.getFileTypeQualifiedName()};
        getFileTypeInfo().setAllowedFileTypeQualifiedNames(fileTypes);
    }

    @Override
    public boolean canConvert(String text) {
        return JsonUtil.isJsonStr(text) || StrUtil.isNotBlank(extractJson(text));
    }

    @Override
    public String convert() {
        String contentStr = getContent();
        if (JsonUtil.isJsonStr(contentStr)) {
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
        // 是否使用全局文本做匹配
        String documentText = editorInfo.getDocumentTextInfo().getDocumentText();
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
                return JsonUtil.extractJsonStr(documentText);
            }
        }

        return JsonUtil.extractJsonStr(text);
    }

}
