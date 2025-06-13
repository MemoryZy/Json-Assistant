package cn.memoryzy.json.service.persistent.converter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.service.persistent.state.AnnouncementStats;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Memory
 * @since 2025/5/27
 */
public class AnnouncementStatsConverter extends Converter<Map<String, AnnouncementStats>> {

    @Override
    public @Nullable Map<String, AnnouncementStats> fromString(@NotNull String value) {
        Map<String, AnnouncementStats> announcementStatsMap = new HashMap<>();
        value = StrUtil.str(Base64.decode(value), StandardCharsets.UTF_8);
        ObjectWrapper jsonObject = JsonUtil.parseObject(value);

        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            ObjectWrapper entryValue = (ObjectWrapper) entry.getValue();
            AnnouncementStats announcementStats = AnnouncementStats.fromMap(entryValue);
            announcementStatsMap.put(entry.getKey(), announcementStats);
        }

        return announcementStatsMap;
    }

    @Override
    public @Nullable String toString(@NotNull Map<String, AnnouncementStats> value) {
        return Base64.encode(JsonUtil.compressJson(value));
    }
}
