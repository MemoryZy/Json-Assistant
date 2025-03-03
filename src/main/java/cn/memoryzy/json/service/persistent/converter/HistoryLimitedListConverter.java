package cn.memoryzy.json.service.persistent.converter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.HistoryLimitedList;
import cn.memoryzy.json.model.JsonEntry;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.util.Json5Util;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

/**
 * @author Memory
 * @since 2024/11/26
 */
public class HistoryLimitedListConverter extends Converter<HistoryLimitedList> {
    private static final Logger LOG = Logger.getInstance(HistoryLimitedListConverter.class);

    @Override
    public @Nullable HistoryLimitedList fromString(@NotNull String value) {
        HistoryLimitedList historyList = new HistoryLimitedList(JsonHistoryPersistentState.LIMIT);

        try {
            // 兼容旧数据，只有一次
            if (Base64.isBase64(value)) {
                value = StrUtil.str(Base64.decode(value), StandardCharsets.UTF_8);
            }

            ArrayWrapper jsonArray = Json5Util.parseArray(value);
            // 应该反着来添加，因 List 以 新-旧 向后排，所以应该保持这个顺序
            // 这里后添加的元素会顶着前面的元素往后，所以先反转 List
            // Collections.reverse(jsonArray);
            for (Object data : jsonArray) {
                ObjectWrapper element = (ObjectWrapper) data;
                JsonEntry entry = JsonEntry.fromMap(element);
                historyList.add(entry);
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }

        return historyList;
    }

    @Override
    public @Nullable String toString(@NotNull HistoryLimitedList value) {
        return Base64.encode(Json5Util.compressJson5(value));
    }

}
