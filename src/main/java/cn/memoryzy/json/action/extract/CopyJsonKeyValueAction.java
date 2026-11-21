package cn.memoryzy.json.action.extract;

import cn.memoryzy.json.util.JsonExtractUtil;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.PsiElement;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.Nullable;

/**
 * 复制光标（或选区）所在的键值对，形如：autoHide: false
 * <p>
 * 与结构树的「复制键值对」保持一致：键不加引号，字符串取出去引号后的内容，
 * 对象与数组先格式化为多行文本（格式化失败时回退为原文）。
 *
 * @author Memory
 * @since 2025/11/21
 */
public class CopyJsonKeyValueAction extends AbstractExtractAction {

    public CopyJsonKeyValueAction() {
        super("action.extract.kv.text", "action.extract.kv.description", null);
    }

    @Override
    protected @Nullable String computeText(@Nullable PsiElement element) {
        JsonProperty property = JsonExtractUtil.findCaretProperty(element);
        if (property == null) {
            return null;
        }

        JsonValue value = property.getValue();
        // 键没有值（例如语法未写完），此时无从复制
        if (value == null) {
            return null;
        }

        return property.getName() + ": " + formatValue(value);
    }

    /**
     * 获取值的文本：对象、数组格式化为多行，其余类型取字面量内容
     *
     * @param value Json 值
     * @return 值文本
     */
    private String formatValue(JsonValue value) {
        if (value instanceof JsonObject || value instanceof JsonArray) {
            String formatted = JsonUtil.formatJson(value.getText());
            if (formatted != null) {
                return formatted;
            }
        }

        return JsonExtractUtil.getValueText(value);
    }

}
