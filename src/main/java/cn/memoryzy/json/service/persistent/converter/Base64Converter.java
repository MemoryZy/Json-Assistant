package cn.memoryzy.json.service.persistent.converter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

/**
 * @author Memory
 * @since 2025/7/8
 */
public class Base64Converter extends Converter<String> {

    @Override
    public @Nullable String fromString(@NotNull String value) {
        if (value.startsWith("BASE64:")) {
            value = value.substring(7);
            return StrUtil.str(Base64.decode(value), StandardCharsets.UTF_8);
        }

        return value;
    }

    @Override
    public @Nullable String toString(@NotNull String value) {
        return StrUtil.isBlank(value) ? null : "BASE64:" + Base64.encode(value);
    }
}
