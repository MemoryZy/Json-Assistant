package cn.memoryzy.json.service.persistent.converter;

import cn.memoryzy.json.service.persistent.state.QueryState;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/12/27
 */
public class QueryStateConverter extends Converter<QueryState> {
    @Override
    public @Nullable QueryState fromString(@NotNull String value) {
        return JsonUtil.toObject(value, QueryState.class);
    }

    @Override
    public @Nullable String toString(@NotNull QueryState value) {
        return JsonUtil.compressJson(value);
    }
}
