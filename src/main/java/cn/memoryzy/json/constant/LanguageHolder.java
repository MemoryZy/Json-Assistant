package cn.memoryzy.json.constant;

import cn.memoryzy.json.enums.FileTypeEnum;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.lang.Language;

/**
 * @author Memory
 * @since 2024/9/21
 */
public interface LanguageHolder {

    Language JSON = PlatformUtil.getLanguage(FileTypeEnum.JSON);

    Language JSON5 = PlatformUtil.getLanguage(FileTypeEnum.JSON5);

    Language XML = PlatformUtil.getLanguage(FileTypeEnum.XML);

    Language YAML = PlatformUtil.getLanguage(FileTypeEnum.YAML);

    Language TOML = PlatformUtil.getLanguage(FileTypeEnum.TOML);

}
