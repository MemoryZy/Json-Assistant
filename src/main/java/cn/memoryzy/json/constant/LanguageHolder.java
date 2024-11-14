package cn.memoryzy.json.constant;

import cn.memoryzy.json.enums.FileTypes;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.lang.Language;

/**
 * @author Memory
 * @since 2024/9/21
 */
public interface LanguageHolder {

    Language JSON = PlatformUtil.getLanguage(FileTypes.JSON);

    Language JSON5 = PlatformUtil.getLanguage(FileTypes.JSON5);

    Language XML = PlatformUtil.getLanguage(FileTypes.XML);

    Language YAML = PlatformUtil.getLanguage(FileTypes.YAML);

    Language TOML = PlatformUtil.getLanguage(FileTypes.TOML);

    Language PROPERTIES = PlatformUtil.getLanguage(FileTypes.PROPERTIES);

}
