package cn.memoryzy.json.service.persistent.converter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.model.LimitedList;
import cn.memoryzy.json.service.persistent.JsonViewerHistoryPersistentState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * @author Memory
 * @since 2024/8/15
 */
public class LimitedListConverter extends Converter<LimitedList<String>> {

    private static final Logger LOG = Logger.getInstance(LimitedListConverter.class);

    @Override
    public @Nullable LimitedList<String> fromString(@NotNull String value) {
        LimitedList<String> limitedList = new LimitedList<>(JsonViewerHistoryPersistentState.HISTORY_LIMIT);
        if (StrUtil.isBlank(value)) {
            return limitedList;
        }

        try {
            JSONArray jsonArray = JSONUtil.parseArray(value);
            // 应该反着来添加，因 List以 新-旧 向后排，所以应该保持这个顺序
            // 这里后添加的元素会顶着前面的元素往后，所以先反转 List
            Collections.reverse(jsonArray);
            for (Object object : jsonArray) {
                limitedList.add(object.toString());
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }

        return limitedList;
    }

    @Override
    public @Nullable String toString(@NotNull LimitedList<String> value) {
        return CollUtil.isNotEmpty(value) ? JSONUtil.toJsonStr(value) : "";
    }
}
