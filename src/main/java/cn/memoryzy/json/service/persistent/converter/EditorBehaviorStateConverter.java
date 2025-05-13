package cn.memoryzy.json.service.persistent.converter;

import cn.memoryzy.json.service.persistent.state.EditorBehaviorState;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/11/18
 */
public class EditorBehaviorStateConverter extends Converter<EditorBehaviorState> {

    @Override
    public @Nullable EditorBehaviorState fromString(@NotNull String value) {
        return JsonUtil.toObject(value, EditorBehaviorState.class);
    }

    @Override
    public @Nullable String toString(@NotNull EditorBehaviorState value) {
        return JsonUtil.compressJson(value);
    }

}
