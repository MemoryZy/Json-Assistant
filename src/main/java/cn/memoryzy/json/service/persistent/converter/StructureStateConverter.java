package cn.memoryzy.json.service.persistent.converter;

import cn.memoryzy.json.service.persistent.state.StructureState;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2025/4/7
 */
public class StructureStateConverter extends Converter<StructureState> {

    @Override
    public @Nullable StructureState fromString(@NotNull String value) {
        return JsonUtil.toObject(value, StructureState.class);
    }

    @Override
    public @Nullable String toString(@NotNull StructureState value) {
        return JsonUtil.compressJson(value);
    }
}
