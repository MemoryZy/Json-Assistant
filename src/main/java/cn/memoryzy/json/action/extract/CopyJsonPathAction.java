package cn.memoryzy.json.action.extract;

import cn.memoryzy.json.util.JsonExtractUtil;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.PsiElement;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.Nullable;

/**
 * 复制光标所在的 Json 路径（如 $.data.list[0].name）
 *
 * @author Memory
 * @since 2025/11/21
 */
public class CopyJsonPathAction extends AbstractExtractAction {

    public CopyJsonPathAction() {
        super("action.extract.path.text", "action.extract.path.description", null);
    }

    @Override
    protected @Nullable String computeText(@Nullable PsiElement element) {
        // 光标停在键名上时，也能拿到该键所在节点的路径
        JsonValue value = JsonExtractUtil.resolveCaretValue(element);
        return JsonExtractUtil.buildPath(value);
    }

}