package cn.memoryzy.json.service.persistent.converter;

import com.intellij.ui.ColorUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * @author Memory
 * @since 2024/11/1
 */
public class ColorConverter extends Converter<Color> {

    @Override
    public @Nullable Color fromString(@NotNull String value) {
        return ColorUtil.fromHex(value);
    }

    @Override
    public @Nullable String toString(@NotNull Color value) {
        return ColorUtil.toHex(value);
    }

}
