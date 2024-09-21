package cn.memoryzy.json.constant;

import cn.memoryzy.json.enums.FileTypeEnum;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.fileTypes.FileType;

/**
 * 以常量方式存储，防止经常反射影响效率
 *
 * @author Memory
 * @since 2024/9/21
 */
public interface FileTypeHolder {

    FileType JSON = PlatformUtil.getFileType(FileTypeEnum.JSON);

    FileType JSON5 = PlatformUtil.getFileType(FileTypeEnum.JSON5);

    FileType XML = PlatformUtil.getFileType(FileTypeEnum.XML);

    FileType YAML = PlatformUtil.getFileType(FileTypeEnum.YAML);

    FileType TOML = PlatformUtil.getFileType(FileTypeEnum.TOML);

}
