package cn.memoryzy.json.constant;

import cn.memoryzy.json.enums.FileTypes;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.fileTypes.FileType;

/**
 * 以常量方式存储，防止经常反射影响效率
 *
 * @author Memory
 * @since 2024/9/21
 */
public interface FileTypeHolder {

    FileType JSON = PlatformUtil.getFileType(FileTypes.JSON);

    FileType JSON5 = PlatformUtil.getFileType(FileTypes.JSON5);

    FileType XML = PlatformUtil.getFileType(FileTypes.XML);

    FileType YAML = PlatformUtil.getFileType(FileTypes.YAML);

    FileType TOML = PlatformUtil.getFileType(FileTypes.TOML);

}
