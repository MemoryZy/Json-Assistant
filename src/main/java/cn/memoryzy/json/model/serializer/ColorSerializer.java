package cn.memoryzy.json.model.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.intellij.ui.ColorUtil;

import java.awt.*;
import java.io.IOException;

/**
 * @author Memory
 * @since 2024/11/19
 */
public class ColorSerializer extends JsonSerializer<Color> {
    @Override
    public void serialize(Color color, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (color == null) {
            jsonGenerator.writeNull();
        } else {
            jsonGenerator.writeString(ColorUtil.toHex(color));
        }
    }
}
