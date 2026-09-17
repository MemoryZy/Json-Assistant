package cn.memoryzy.json.service.persistent.state;

import cn.memoryzy.json.enums.DataFormatType;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Memory
 * @since 2025/12/15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRecord extends BaseData {

    /**
     * 记录短文本（展示）
     */
    private String displayText;

    /**
     * 原文类型
     */
    private DataFormatType sourceType;

    /**
     * 文件后缀名
     */
    private String fileExtension;

    /**
     * 记录解析后的 JSON 对象
     */
    @Nullable
    @JsonIgnore
    private JsonWrapper wrapper;

    /**
     * 记录源文件
     */
    @JsonIgnore
    private VirtualFile sourceFile;


    public String getDisplayText() {
        return displayText;
    }

    public DataFormatType getSourceType() {
        return sourceType;
    }

    @JsonIgnore
    public @Nullable JsonWrapper getWrapper() {
        return wrapper;
    }

    @JsonIgnore
    public VirtualFile getSourceFile() {
        return sourceFile;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public void setSourceType(DataFormatType sourceType) {
        this.sourceType = sourceType;
    }

    @JsonIgnore
    public void setWrapper(@Nullable JsonWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @JsonIgnore
    public void setSourceFile(VirtualFile sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        JsonRecord record = (JsonRecord) o;
        return Objects.equals(displayText, record.displayText) && sourceType == record.sourceType && Objects.equals(wrapper, record.wrapper) && Objects.equals(sourceFile, record.sourceFile) && Objects.equals(fileExtension, record.fileExtension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayText, sourceType, wrapper, sourceFile, fileExtension);
    }
}
