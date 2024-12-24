package cn.memoryzy.json.model.jackson;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.intellij.ui.ColorUtil;

import java.awt.*;
import java.io.IOException;

/**
 * @author Memory
 * @since 2024/11/19
 */
public class ColorDeserializer extends JsonDeserializer<Color> {
    @Override
    public Color deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String text = StrUtil.trim(jsonParser.getText());
        return StrUtil.isBlank(text) ? null : ColorUtil.fromHex(text);
    }
}
