package cn.memoryzy.json.service.persistent.converter;

import cn.memoryzy.json.service.persistent.state.DeserializerState;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/12/23
 */
public class DeserializerStateConverter extends Converter<DeserializerState> {

    @Override
    public @Nullable DeserializerState fromString(@NotNull String value) {
        return JsonUtil.toObject(value, DeserializerState.class);
    }

    @Override
    public @Nullable String toString(@NotNull DeserializerState value) {
        return JsonUtil.compressJson(value);
    }
}
