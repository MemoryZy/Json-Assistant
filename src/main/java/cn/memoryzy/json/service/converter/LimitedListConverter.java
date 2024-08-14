package cn.memoryzy.json.service.converter;

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
        JSONArray jsonArray = JSONUtil.parseArray(value);

        LimitedList<String> limitedList = new LimitedList<>(JsonViewerHistoryState.HISTORY_LIMIT);
        for (Object object : jsonArray) {
            limitedList.add(object.toString());
        }

        return limitedList;
    }

    @Override
    public @Nullable String toString(@NotNull LimitedList<String> value) {
        return JSONUtil.toJsonStr(value);
    }
}
