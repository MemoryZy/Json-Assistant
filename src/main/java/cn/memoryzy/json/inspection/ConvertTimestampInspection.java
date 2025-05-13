package cn.memoryzy.json.inspection;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonAssistantUtil;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Memory
 * @since 2025/2/24
 */
public class ConvertTimestampInspection extends LocalInspectionTool {

    private static final Logger LOG = Logger.getInstance(ConvertTimestampInspection.class);

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
        // 判断是否为 String、Number 类型
        if (jsonValue instanceof JsonStringLiteral) {
            String value = ((JsonStringLiteral) jsonValue).getValue();
            // 若为有效时间戳
            if (JsonAssistantUtil.isValidTimestamp(value)) {
                holder.registerProblem(jsonValue, JsonAssistantBundle.messageOnSystem("inspection.convert.timestamp.description"), new ConvertTimestampFix(jsonValue));
            }

        } else if (jsonValue instanceof JsonNumberLiteral) {
            // double长数值默认表示为科学计数法形式，需转为原样
            String valueText = jsonValue.getText();
            BigDecimal bigDecimalValue = JsonAssistantUtil.parseNumber(valueText);
            long longValue = bigDecimalValue.longValue();

            // 若为有效时间戳
            if (JsonAssistantUtil.isValidTimestamp(longValue + "")) {
                holder.registerProblem(jsonValue, JsonAssistantBundle.messageOnSystem("inspection.convert.timestamp.description"), new ConvertTimestampFix(jsonValue));
            }
        }
    }



    public static class ConvertTimestampFix extends LocalQuickFixAndIntentionActionOnPsiElement {

        private ConvertTimestampFix(@Nullable PsiElement element) {
            super(element);
        }

        @Override
        public void invoke(@NotNull Project project, @NotNull PsiFile file, @Nullable Editor editor, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
            if (editor == null) return;

            long timestamp = 0;
            if (startElement instanceof JsonStringLiteral) {
                String value = ((JsonStringLiteral) startElement).getValue();
                timestamp = Long.parseLong(value);
            } else if (startElement instanceof JsonNumberLiteral) {
                double value = ((JsonNumberLiteral) startElement).getValue();
                timestamp = (long) value;
            }

            if (!JsonAssistantUtil.isValidTimestamp(timestamp + "")) {
                ConvertTimestampInspection.LOG.error("The timestamp format is incorrect: " + timestamp);
                return;
            }

            String time = JsonAssistantUtil.formatDateBasedOnTimestampDetails(timestamp);
            time = "\"" + time + "\"";
            startElement.replace(new JsonElementGenerator(project).createValue(time));
        }

        @Override
        public @NotNull String getText() {
            return JsonAssistantBundle.message("inspection.convert.timestamp.text");
        }

        @Override
        public @NotNull String getFamilyName() {
            return JsonAssistantBundle.message("inspection.convert.timestamp.familyName");
        }
    }
}
