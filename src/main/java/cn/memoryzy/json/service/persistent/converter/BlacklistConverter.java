package cn.memoryzy.json.service.persistent.converter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.JsonEntry;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.util.Json5Util;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

/**
 * @author Memory
 * @since 2025/3/3
 */
public class BlacklistConverter extends Converter<LinkedList<JsonEntry>> {

    @Override
    public @Nullable LinkedList<JsonEntry> fromString(@NotNull String value) {
        LinkedList<JsonEntry> jsonEntries = new LinkedList<>();
        value = StrUtil.str(Base64.decode(value), StandardCharsets.UTF_8);
        ArrayWrapper jsonArray = Json5Util.parseArray(value);
        for (Object data : jsonArray) {
            ObjectWrapper element = (ObjectWrapper) data;
            JsonEntry entry = JsonEntry.fromMap(element);
            jsonEntries.add(entry);
        }

        return jsonEntries;
    }

    @Override
    public @Nullable String toString(@NotNull LinkedList<JsonEntry> value) {
        return Base64.encode(Json5Util.compressJson5(value));
    }
}
