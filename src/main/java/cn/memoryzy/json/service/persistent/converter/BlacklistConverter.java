package cn.memoryzy.json.service.persistent.converter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.service.persistent.state.BlacklistEntry;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

/**
 * @author Memory
 * @since 2025/3/3
 */
public class BlacklistConverter extends Converter<LinkedList<BlacklistEntry>> {

    @Override
    public @Nullable LinkedList<BlacklistEntry> fromString(@NotNull String value) {
        LinkedList<BlacklistEntry> jsonEntries = new LinkedList<>();
        value = StrUtil.str(Base64.decode(value), StandardCharsets.UTF_8);

        // 因混淆的原因，不能用 JSON5 存储
        ArrayWrapper jsonArray = Json5Util.isJson5(value) ? Json5Util.parseArray(value) : JsonUtil.parseArray(value);

        for (Object data : jsonArray) {
            ObjectWrapper element = (ObjectWrapper) data;
            BlacklistEntry entry = BlacklistEntry.fromMap(element);
            jsonEntries.add(entry);
        }

        return jsonEntries;
    }

    @Override
    public @Nullable String toString(@NotNull LinkedList<BlacklistEntry> value) {
        return Base64.encode(JsonUtil.compressJson(value));
    }
}
