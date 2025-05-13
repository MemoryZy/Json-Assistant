package cn.memoryzy.json.service.persistent.converter;

import cn.memoryzy.json.service.persistent.state.AttributeSerializationState;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/11/18
 */
public class AttributeSerializationStateConverter extends Converter<AttributeSerializationState> {

    @Override
    public @Nullable AttributeSerializationState fromString(@NotNull String value) {
        return JsonUtil.toObject(value, AttributeSerializationState.class);
    }

    @Override
    public @Nullable String toString(@NotNull AttributeSerializationState value) {
        return JsonUtil.compressJson(value);
    }

}
