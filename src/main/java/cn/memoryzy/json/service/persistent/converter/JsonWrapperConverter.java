package cn.memoryzy.json.service.persistent.converter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

/**
 * @author Memory
 * @since 2025/6/18
 */
public class JsonWrapperConverter extends Converter<JsonWrapper> {

    @Override
    public @Nullable JsonWrapper fromString(@NotNull String value) {
        value = StrUtil.str(Base64.decode(value), StandardCharsets.UTF_8);
        return JsonUtil.parse(value);
    }

    @Override
    public @Nullable String toString(@NotNull JsonWrapper value) {
        // TODO 需要加密存储
        return Base64.encode(JsonUtil.compressJson(value));
    }
}
