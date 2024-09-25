package cn.memoryzy.json.action;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.util.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.ConstantExpressionEvaluator;
import com.intellij.psi.impl.LanguageConstantExpressionEvaluator;
import com.intellij.psi.util.PsiTreeUtil;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/9/6
 */
public class JavaConstantExtractToJsonAction extends AnAction {

    public JavaConstantExtractToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.extractor.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.extractor.description"));
        presentation.setIcon(PlatformUtil.isNewUi() ? JsonAssistantIcons.ExpUi.NEW_EXTRACT : JsonAssistantIcons.EXTRACT);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        // 不做方法返回值的处理
        PsiElement element = PlatformUtil.getPsiElementByOffset(e);
        PsiField psiField = PsiTreeUtil.getParentOfType(element, PsiField.class);
        PsiLocalVariable localVariable = PsiTreeUtil.getParentOfType(element, PsiLocalVariable.class);

        String jsonStr = null;
        if (null != psiField) {
            String canonicalText = psiField.getType().getCanonicalText();
            if (String.class.getName().equals(canonicalText)) {
                jsonStr = computeConstantExpression(psiField);
            }
        } else if (null != localVariable) {
            String canonicalText = localVariable.getType().getCanonicalText();
            if (String.class.getName().equals(canonicalText)) {
                jsonStr = computeConstantExpression(localVariable);
            }
        }

        if (StrUtil.isBlank(jsonStr)) return;
        if (!JsonUtil.isJsonStr(jsonStr)) return;

        try {
            JsonAssistantUtil.addNewContentWithEditorContentIfNeeded(project, jsonStr, FileTypeHolder.JSON);
        } catch (Exception ex) {
            PlatformUtil.setClipboard(jsonStr);
            Notifications.showNotification(JsonAssistantBundle.messageOnSystem("notify.no.write.json.copy.text"), NotificationType.INFORMATION, project);
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void update(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        PsiElement element = PlatformUtil.getPsiElementByOffset(e);
        PsiField psiField = PsiTreeUtil.getParentOfType(element, PsiField.class);
        PsiLocalVariable localVariable = PsiTreeUtil.getParentOfType(element, PsiLocalVariable.class);

        String jsonStr = null;
        if (null != psiField) {
            String canonicalText = psiField.getType().getCanonicalText();
            if (String.class.getName().equals(canonicalText)) {
                jsonStr = computeConstantExpression(psiField);
            }
        } else if (null != localVariable) {
            String canonicalText = localVariable.getType().getCanonicalText();
            if (String.class.getName().equals(canonicalText)) {
                jsonStr = computeConstantExpression(localVariable);
            }
        }

        e.getPresentation().setEnabledAndVisible(project != null && StrUtil.isNotBlank(jsonStr) && JsonUtil.isJsonStr(jsonStr));
    }


    private String computeConstantExpression(PsiField field) {
        return computeInitializer(field.getInitializer());
    }

    private String computeConstantExpression(PsiLocalVariable localVariable) {
        return computeInitializer(localVariable.getInitializer());
    }


    /**
     * 计算初始化常量值
     *
     * @param initializer 初始化常量表达式
     * @return 结果
     */
    private String computeInitializer(PsiExpression initializer) {
        if (initializer instanceof PsiLiteralExpression) {
            // 单独的 Java 字面表达式
            return computeLiteralExpression((PsiLiteralExpression) initializer);

        } else if (initializer instanceof PsiBinaryExpression) {
            // Java二进制表达式（加、乘等）
            return computeBinaryExpression((PsiBinaryExpression) initializer);

        } else if (initializer instanceof PsiPolyadicExpression) {
            // 多表达式拼接
            return computePolyadicExpression((PsiPolyadicExpression) initializer);

        } else if (initializer instanceof PsiReferenceExpression) {
            // 单引用参数
            return computeReferenceExpression((PsiReferenceExpression) initializer);
        }

        return null;
    }


    /**
     * 计算 Java 文字表达式结果（一般为 String、char、int, long, float, double、boolean 等）
     *
     * @param literalExpression 字面表达式
     * @return 结果
     */
    private String computeLiteralExpression(PsiLiteralExpression literalExpression) {
        return String.valueOf(literalExpression.getValue());
    }


    /**
     * 计算 Java 二进制表达式结果（加法、乘法等）
     *
     * @param binaryExpression 二进制表达式
     * @return 结果
     */
    private String computeBinaryExpression(PsiBinaryExpression binaryExpression) {
        PsiExpression lOperand = binaryExpression.getLOperand();
        PsiExpression rOperand = binaryExpression.getROperand();

        // 在此不计算方法调用表达式，防止调用过深
        if ((lOperand instanceof PsiMethodCallExpression) || (rOperand instanceof PsiMethodCallExpression)) {
            return null;
        }

        // 皆为字面表达式
        if ((lOperand instanceof PsiLiteralExpression) && (rOperand instanceof PsiLiteralExpression)) {
            return String.valueOf(((PsiLiteralExpression) lOperand).getValue()) + ((PsiLiteralExpression) rOperand).getValue();
        }

        // 若存在引用表达式
        if ((lOperand instanceof PsiReferenceExpression) || (rOperand instanceof PsiReferenceExpression)) {
            PsiExpression[] expressions = new PsiExpression[]{lOperand, rOperand};
            return computeMultiExpression(expressions);
        }

        return null;
    }

    /**
     * 计算多元表达式结果（多种表达式计算）
     *
     * @param polyadicExpression 多元表达式
     * @return 结果
     */
    private String computePolyadicExpression(PsiPolyadicExpression polyadicExpression) {
        PsiExpression[] expressions = polyadicExpression.getOperands();
        // 判断是否皆为 PsiLiteralExpression，且字面表达式的值皆不为 null，如果是，直接拼接
        if (Arrays.stream(expressions).allMatch(el -> (el instanceof PsiLiteralExpression) && Objects.nonNull(PsiUtil.computeLiteralExpression((PsiLiteralExpression) el)))) {
            ConstantExpressionEvaluator constantExpressionEvaluator = LanguageConstantExpressionEvaluator.INSTANCE.forLanguage(polyadicExpression.getLanguage());
            return (String) constantExpressionEvaluator.computeConstantExpression(polyadicExpression, true);
        } else if (Arrays.stream(expressions).anyMatch(el -> el instanceof PsiMethodCallExpression)) {
            // 在此不计算方法调用表达式，防止调用过深
            return null;
        } else {
            return computeMultiExpression(expressions);
        }
    }

    /**
     * 计算引用表达式
     *
     * @param referenceExpression 引用表达式
     * @return 值
     */
    private String computeReferenceExpression(PsiReferenceExpression referenceExpression) {
        return computeMultiExpression(new PsiExpression[]{referenceExpression});
    }

    /**
     * 计算字面表达式或引用表达式混杂的结果
     *
     * @param expressions 表达式列表
     * @return 结果
     */
    private String computeMultiExpression(PsiExpression[] expressions) {
        List<String> resultList = new ArrayList<>();
        computeMultiExpression(expressions, resultList);
        return StrUtil.join("", resultList);
    }

    /**
     * 递归计算字面表达式或引用表达式混杂的结果
     *
     * @param expressions 表达式列表
     * @param resultList  结果列表
     */
    private void computeMultiExpression(PsiExpression[] expressions, List<String> resultList) {
        for (PsiExpression expression : expressions) {
            if (expression instanceof PsiLiteralExpression) {
                resultList.add(String.valueOf(PsiUtil.computeLiteralExpression((PsiLiteralExpression) expression)));

            } else if (expression instanceof PsiReferenceExpression) {
                PsiReferenceExpression referenceExpression = (PsiReferenceExpression) expression;
                PsiElement resolve = referenceExpression.resolve();
                if (resolve instanceof PsiField) {
                    resultList.add(computeConstantExpression((PsiField) resolve));
                } else if (resolve instanceof PsiLocalVariable) {
                    resultList.add(computeConstantExpression((PsiLocalVariable) resolve));
                }
            }
        }
    }

}
