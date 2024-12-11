package cn.memoryzy.json.service.persistent.converter;

import cn.memoryzy.json.service.persistent.state.GeneralState;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/12/11
 */
public class GeneralStateConverter extends Converter<GeneralState> {

    @Override
    public @Nullable GeneralState fromString(@NotNull String value) {
        return JsonUtil.toObject(value, GeneralState.class);
    }

    @Override
    public @Nullable String toString(@NotNull GeneralState value) {
        return JsonUtil.compressJson(value);
    }

}
