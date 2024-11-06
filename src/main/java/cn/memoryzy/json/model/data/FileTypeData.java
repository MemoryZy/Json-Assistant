package cn.memoryzy.json.model.data;

import com.intellij.openapi.fileTypes.FileType;

/**
 * 文件类型所代表的类型信息
 *
 * @author Memory
 * @since 2024/11/3
 */
public class FileTypeData {

    /**
     * 转换完成后的文本代表的文件类型
     */
    private FileType processedFileType;

    /**
     * 支持写入的文件类型，类全限定名（通常只是 JSON 类型赋此值，其他类型可置为 null）
     *
     * <p>解析出文本后，要写入到文档中，若文档类型符合在此指定的类型，则直接写入，反之做其他处理</p>
     */
    private String[] allowedFileTypeQualifiedNames;


    // ----------------------- GETTER/SETTER -----------------------

    public FileType getProcessedFileType() {
        return processedFileType;
    }

    public FileTypeData setProcessedFileType(FileType processedFileType) {
        this.processedFileType = processedFileType;
        return this;
    }

    public String[] getAllowedFileTypeQualifiedNames() {
        return allowedFileTypeQualifiedNames;
    }

    public FileTypeData setAllowedFileTypeQualifiedNames(String[] allowedFileTypeQualifiedNames) {
        this.allowedFileTypeQualifiedNames = allowedFileTypeQualifiedNames;
        return this;
    }
}
