package cn.memoryzy.json.service.persistent.converter;

import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2025/6/18
 */
public class JsonWrapperConverter extends Converter<JsonWrapper> {

    @Override
    public @Nullable JsonWrapper fromString(@NotNull String value) {
        return JsonUtil.parse(JsonAssistantUtil.decodeAndDecompress(value));
    }

    @Override
    public @Nullable String toString(@NotNull JsonWrapper value) {
        return JsonAssistantUtil.compressAndEncode(JsonUtil.compressJson(value));
    }
}
