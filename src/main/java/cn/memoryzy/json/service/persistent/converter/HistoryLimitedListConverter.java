package cn.memoryzy.json.service.persistent.converter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.service.persistent.state.HistoryLimitedList;
import cn.memoryzy.json.service.persistent.state.JsonEntry;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

/**
 * @author Memory
 * @since 2024/11/26
 */
public class HistoryLimitedListConverter extends Converter<HistoryLimitedList> {

    @Override
    public @Nullable HistoryLimitedList fromString(@NotNull String value) {
        HistoryLimitedList historyList = new HistoryLimitedList(JsonHistoryPersistentState.LIMIT);

        // 兼容旧数据，只有一次
        if (Base64.isBase64(value)) {
            value = StrUtil.str(Base64.decode(value), StandardCharsets.UTF_8);
        }

        // 因混淆的原因，不能用 JSON5 存储
        ArrayWrapper jsonArray = Json5Util.isJson5(value) ? Json5Util.parseArray(value) : JsonUtil.parseArray(value);

        // 应该反着来添加，因 List 以 新-旧 向后排，所以应该保持这个顺序
        // 这里后添加的元素会顶着前面的元素往后，所以先反转 List
        // Collections.reverse(jsonArray);
        for (Object data : jsonArray) {
            ObjectWrapper element = (ObjectWrapper) data;
            Long insertTime = null;
            String time = element.get("insertTime") + "";
            if (!JsonAssistantUtil.isValidTimestamp(time)) {
                DateTime dateTime = null;
                try {
                    dateTime = DateUtil.parse(time);
                } catch (Exception ignored) {
                }

                if (null != dateTime) {
                    insertTime = dateTime.getTime();
                }
            }

            JsonEntry entry = BeanUtil.toBean(element, JsonEntry.class);
            if (null != insertTime) entry.setInsertTime(insertTime);

            historyList.add(entry);
        }

        return historyList;
    }

    @Override
    public @Nullable String toString(@NotNull HistoryLimitedList value) {
        return Base64.encode(JsonUtil.compressJson(value));
    }

}
