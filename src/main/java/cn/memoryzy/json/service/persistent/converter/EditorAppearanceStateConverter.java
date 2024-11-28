package cn.memoryzy.json.service.persistent.converter;

import cn.memoryzy.json.service.persistent.state.EditorAppearanceState;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/11/18
 */
public class EditorAppearanceStateConverter extends Converter<EditorAppearanceState> {

    @Override
    public @Nullable EditorAppearanceState fromString(@NotNull String value) {
        return JsonUtil.toObject(value, EditorAppearanceState.class);
    }

    @Override
    public @Nullable String toString(@NotNull EditorAppearanceState value) {
        // Jackson无法序列化Color类，需手动转换
        return JsonUtil.compressJson(value);
    }

}
