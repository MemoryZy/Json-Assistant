package cn.memoryzy.json.inspection;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.json.psi.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Memory
 * @since 2025/1/22
 */
public class ExpandNestedJsonInspection extends LocalInspectionTool {

    private static final Logger LOG = Logger.getInstance(ExpandNestedJsonInspection.class);

    @SuppressWarnings("DuplicatedCode")
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JsonElementVisitor() {
            @Override
            public void visitObject(@NotNull JsonObject o) {
                List<JsonProperty> propertyList = o.getPropertyList();
                for (JsonProperty property : propertyList) {
                    JsonValue jsonValue = property.getValue();
                    registerProblem(jsonValue, holder);
                }
            }

            @Override
            public void visitArray(@NotNull JsonArray o) {
                List<JsonValue> valueList = o.getValueList();
                for (JsonValue jsonValue : valueList) {
                    registerProblem(jsonValue, holder);
                }
            }
        };
    }

    private void registerProblem(JsonValue jsonValue, ProblemsHolder holder) {
        // 判断是否为 String 类型
        if (jsonValue instanceof JsonStringLiteral) {
            String value = ((JsonStringLiteral) jsonValue).getValue();
            // 若为 JSON 格式
            if (StrUtil.isNotBlank(value) && (JsonUtil.isJson(value) || Json5Util.isJson5(value))) {
                holder.registerProblem(jsonValue, JsonAssistantBundle.messageOnSystem("inspection.expand.json.description"), new ExpandNestedJsonFix(jsonValue));
            }
        }
    }

    public static class ExpandNestedJsonFix extends LocalQuickFixAndIntentionActionOnPsiElement {

        private ExpandNestedJsonFix(@Nullable PsiElement element) {
            super(element);
        }

        @Override
        public void invoke(@NotNull Project project,
                           @NotNull PsiFile file,
                           @Nullable Editor editor,
                           @NotNull PsiElement startElement,
                           @NotNull PsiElement endElement) {
            if (editor == null) return;

            String value = ((JsonStringLiteral) startElement).getValue();
            String formatted = JsonUtil.isJson(value) ? JsonUtil.formatJson(value) : Json5Util.formatJson5(value);
            if (StrUtil.isBlank(formatted)) {
                ExpandNestedJsonInspection.LOG.error("Formatting failure, original: " + value);
                return;
            }

            startElement.replace(new JsonElementGenerator(project).createValue(formatted));
        }

        @Override
        public @NotNull String getText() {
            return JsonAssistantBundle.message("inspection.expand.json.text");
        }

        @Override
        public @NotNull String getFamilyName() {
            return getText();
        }
    }


}
