package cn.memoryzy.json.service.persistent.converter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.BlacklistEntry;
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
public class BlacklistConverter extends Converter<LinkedList<BlacklistEntry>> {

    @Override
    public @Nullable LinkedList<BlacklistEntry> fromString(@NotNull String value) {
        LinkedList<BlacklistEntry> jsonEntries = new LinkedList<>();
        value = StrUtil.str(Base64.decode(value), StandardCharsets.UTF_8);
        ArrayWrapper jsonArray = Json5Util.parseArray(value);
        for (Object data : jsonArray) {
            ObjectWrapper element = (ObjectWrapper) data;
            BlacklistEntry entry = BlacklistEntry.fromMap(element);
            jsonEntries.add(entry);
        }

        return jsonEntries;
    }

    @Override
    public @Nullable String toString(@NotNull LinkedList<BlacklistEntry> value) {
        return Base64.encode(Json5Util.compressJson5(value));
    }
}
