package cn.memoryzy.json.action.extract;

import cn.memoryzy.json.util.JsonExtractUtil;
import com.intellij.json.psi.JsonProperty;
import com.intellij.psi.PsiElement;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.Nullable;

/**
 * 复制光标（或选区）所在的键
 * <p>
 * 光标落在键、冒号或值上时，都取该属性的键名；数组元素没有键，此时隐藏。
 *
 * @author Memory
 * @since 2025/11/21
 */
public class CopyJsonKeyAction extends AbstractExtractAction {

    public CopyJsonKeyAction() {
        super("action.extract.key.text", "action.extract.key.description", JsonAssistantIcons.Structure.JSON_KEY);
    }

    @Override
    protected @Nullable String computeText(@Nullable PsiElement element) {
        JsonProperty property = JsonExtractUtil.findCaretProperty(element);
        // 数组元素没有键，getName() 为空时同样视为不可提取
        return property == null ? null : property.getName();
    }

}