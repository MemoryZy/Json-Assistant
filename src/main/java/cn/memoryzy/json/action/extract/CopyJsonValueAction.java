package cn.memoryzy.json.action.extract;

import cn.memoryzy.json.util.JsonExtractUtil;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.PsiElement;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.Nullable;

/**
 * 复制光标（或选区）所在的值
 * <p>
 * 光标或选区落在键上时，复制该键对应的值；字符串取出去引号后的内容，与结构树的「复制值」保持一致。
 *
 * @author Memory
 * @since 2025/11/21
 */
public class CopyJsonValueAction extends AbstractExtractAction {

    public CopyJsonValueAction() {
        super("action.extract.value.text", "action.extract.value.description", null);
    }

    @Override
    protected @Nullable String computeText(@Nullable PsiElement element) {
        JsonValue value = JsonExtractUtil.findCaretValue(element);
        return JsonExtractUtil.getValueText(value);
    }

}