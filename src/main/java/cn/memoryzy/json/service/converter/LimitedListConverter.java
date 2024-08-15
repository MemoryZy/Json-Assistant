package cn.memoryzy.json.service.converter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.models.LimitedList;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/8/15
 */
public class LimitedListConverter extends Converter<LimitedList<String>> {

    @Override
    public @Nullable LimitedList<String> fromString(@NotNull String value) {
        LimitedList<String> limitedList = new LimitedList<>(JsonViewerHistoryState.HISTORY_LIMIT);
        if (StrUtil.isBlank(value)) {
            return limitedList;
        }

        try {
            JSONArray jsonArray = JSONUtil.parseArray(value);
            for (Object object : jsonArray) {
                limitedList.add(object.toString());
            }
        } catch (Exception ignored) {
        }

        return limitedList;
    }

    @Override
    public @Nullable String toString(@NotNull LimitedList<String> value) {
        return CollUtil.isNotEmpty(value) ? JSONUtil.toJsonStr(value) : "";
    }
}
