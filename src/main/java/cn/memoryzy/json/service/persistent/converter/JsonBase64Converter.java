package cn.memoryzy.json.service.persistent.converter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

/**
 * Json文本 <--> Base64
 * <p>防止 Json 文本中存在一些 XML 中的非法字符</p>
 *
 * @author Memory
 * @since 2025/6/20
 */
public class JsonBase64Converter extends Converter<String> {

    /**
     * 从 Base64 转为 Json
     *
     * @param base64Str 文本
     * @return Json文本
     */
    @Override
    public @Nullable String fromString(@NotNull String base64Str) {
        return StrUtil.str(Base64.decode(base64Str), StandardCharsets.UTF_8);
    }

    /**
     * 从 Json 转为 Base64
     *
     * @param jsonStr 文本
     * @return Base64文本
     */
    @Override
    public @Nullable String toString(@NotNull String jsonStr) {
        return Base64.encode(jsonStr);
    }

}
