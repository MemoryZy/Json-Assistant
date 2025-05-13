package cn.memoryzy.json.intention;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.json.psi.JsonElementGenerator;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Objects;

/**
 * @author Memory
 * @since 2025/3/31
 */
public class ConvertReadableTimeIntention extends PsiElementBaseIntentionAction implements IntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        JsonStringLiteral jsonStringLiteral = PsiTreeUtil.getParentOfType(element, JsonStringLiteral.class);
        if (Objects.nonNull(jsonStringLiteral)) {
            String value = jsonStringLiteral.getValue();
            if (StrUtil.isNotBlank(value) && !JsonAssistantUtil.isValidTimestamp(value)) {
                // 是否可以被转为时间
                Date date = JsonAssistantUtil.getDate(value);
                if (Objects.nonNull(date)) {
                    long timestamp = date.getTime();
                    jsonStringLiteral.replace(new JsonElementGenerator(project).createValue(timestamp + ""));
                }
            }
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        JsonStringLiteral jsonStringLiteral = PsiTreeUtil.getParentOfType(element, JsonStringLiteral.class);
        if (Objects.nonNull(jsonStringLiteral)) {
            String value = jsonStringLiteral.getValue();
            if (StrUtil.isNotBlank(value) && !JsonAssistantUtil.isValidTimestamp(value)) {
                // 是否可以被转为时间，并且本身不为时间戳
                return Objects.nonNull(JsonAssistantUtil.getDate(value));
            }
        }

        return false;
    }

    /**
     * Returns text for name of this family of intentions. It is used to externalize
     * "auto-show" state of intentions.
     * It is also the directory name for the descriptions.
     *
     * @return  the intention family name.
     */
    @Override
    public @NotNull String getFamilyName() {
        return JsonAssistantBundle.message("intention.convert.to.timestamp.familyName");
    }

    /**
     * If this action is applicable, returns the text to be shown in the list of
     * intention actions available.
     */
    @Override
    public @NotNull String getText() {
        return JsonAssistantBundle.message("intention.convert.to.timestamp.text");
    }

    /**
     * Indicates this intention action expects the Psi framework to provide the write action
     * context for any changes.
     *
     * @return <ul>
     * <li> true if the intention requires a write action context to be provided</li>
     * <li> false if this intention action will start a write action</li>
     * </ul>
     */
    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
