package cn.memoryzy.json.model.serializer;

import a2u.tn.utils.json.TnJsonBuilder;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;

import java.time.LocalDateTime;

/**
 * Json5 序列化时对 LocalDateTime 类型做处理
 *
 * @author Memory
 * @since 2024/11/25
 */
public class LocalDateTimeTypeHandler implements TnJsonBuilder.ITypeHandler {
    @Override
    public Object handleType(Object value) {
        return value instanceof LocalDateTime
                ? LocalDateTimeUtil.format((LocalDateTime) value, DatePattern.NORM_DATETIME_PATTERN)
                : value;
    }
}