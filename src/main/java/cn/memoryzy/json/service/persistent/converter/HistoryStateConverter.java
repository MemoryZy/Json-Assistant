package cn.memoryzy.json.service.persistent.converter;

import cn.memoryzy.json.service.persistent.state.HistoryState;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/11/29
 */
public class HistoryStateConverter extends Converter<HistoryState>  {

    @Override
    public @Nullable HistoryState fromString(@NotNull String value) {
        return JsonUtil.toObject(value, HistoryState.class);
    }

    @Override
    public @Nullable String toString(@NotNull HistoryState value) {
        return JsonUtil.compressJson(value);
    }

}
