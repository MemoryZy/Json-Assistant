package cn.memoryzy.json.service.persistent.converter;

import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 压缩文本
 *
 * @author Memory
 * @since 2025/7/8
 */
public class CompressConverter extends Converter<String> {

    @Override
    public @Nullable String fromString(@NotNull String compressText) {
        // 可能是压缩文本，也可能是Base64处理过后的
        return JsonAssistantUtil.decodeAndDecompress(compressText);
    }

    @Override
    public @Nullable String toString(@NotNull String originalText) {
        return JsonAssistantUtil.compressAndEncode(originalText);
    }
}
