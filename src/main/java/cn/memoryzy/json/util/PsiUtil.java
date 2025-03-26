package cn.memoryzy.json.util;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.*;
import com.intellij.psi.impl.ConstantExpressionEvaluator;
import com.intellij.psi.impl.LanguageConstantExpressionEvaluator;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Memory
 * @since 2024/9/6
 */
public class PsiUtil {

    /**
     * 计算 Java 文字表达式结果（一般为 String、char、int, long, float, double、boolean 等）
     *
     * @param literalExpression 字面表达式
     * @return 结果
     */
    public static Object computeLiteralExpression(PsiLiteralExpression literalExpression) {
        return Objects.isNull(literalExpression) ? null : literalExpression.getValue();
    }

    public static String computeStringExpression(DataContext dataContext) {
        String jsonStr = null;
        // 不做方法返回值的处理
        PsiElement element = PlatformUtil.getPsiElementByOffset(dataContext);
        PsiField psiField = PsiTreeUtil.getParentOfType(element, PsiField.class);
        PsiLocalVariable localVariable = PsiTreeUtil.getParentOfType(element, PsiLocalVariable.class);
        PsiPolyadicExpression psiPolyadicExpression = PsiTreeUtil.getParentOfType(element, PsiPolyadicExpression.class);

        if (Objects.isNull(psiField) && Objects.isNull(localVariable) && Objects.isNull(psiPolyadicExpression)) {
            PsiLiteralExpression psiLiteralExpression = PsiTreeUtil.getParentOfType(element, PsiLiteralExpression.class);
            if (element instanceof PsiJavaToken) {
                // 处理Java文本块和Java单行文本
                jsonStr = String.valueOf(computeLiteralExpression(psiLiteralExpression));
            }

        } else {
            if (null != psiPolyadicExpression) {
                jsonStr = String.valueOf(computePolyadicExpression(psiPolyadicExpression));

            } else if (null != psiField) {
                String canonicalText = psiField.getType().getCanonicalText();
                jsonStr = String.class.getName().equals(canonicalText) ? String.valueOf(computeConstantExpression(psiField)) : null;

            } else {
                String canonicalText = localVariable.getType().getCanonicalText();
                jsonStr = String.class.getName().equals(canonicalText) ? String.valueOf(computeConstantExpression(localVariable)) : null;
            }
        }

        return StrUtil.isBlank(jsonStr) || Objects.equals("null", jsonStr) ? null : jsonStr;
    }

    public static Object computeConstantExpression(PsiField field) {
        return computeInitializer(field.getInitializer());
    }

    public static Object computeConstantExpression(PsiLocalVariable localVariable) {
        return computeInitializer(localVariable.getInitializer());
    }


    /**
     * 计算初始化常量值
     *
     * @param initializer 初始化常量表达式
     * @return 结果
     */
    public static Object computeInitializer(PsiExpression initializer) {
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
     * 计算 Java 二进制表达式结果（加法、乘法等）
     *
     * @param binaryExpression 二进制表达式
     * @return 结果
     */
    private static Object computeBinaryExpression(PsiBinaryExpression binaryExpression) {
        // 左边指定的常量值
        PsiExpression lOperand = binaryExpression.getLOperand();
        // 右边指定的常量值
        PsiExpression rOperand = binaryExpression.getROperand();

        // 在此不计算方法调用表达式，防止调用过深
        if ((lOperand instanceof PsiMethodCallExpression) || (rOperand instanceof PsiMethodCallExpression)) {
            return null;
        }

        // 皆为字面表达式
        if ((lOperand instanceof PsiLiteralExpression) && (rOperand instanceof PsiLiteralExpression)) {
            Object lValue = ((PsiLiteralExpression) lOperand).getValue();
            Object rValue = ((PsiLiteralExpression) rOperand).getValue();
            return combineValues(lValue, rValue);
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
    private static Object computePolyadicExpression(PsiPolyadicExpression polyadicExpression) {
        PsiExpression[] expressions = polyadicExpression.getOperands();
        Predicate<PsiExpression> predicate = el -> (el instanceof PsiLiteralExpression)
                && Objects.nonNull(PsiUtil.computeLiteralExpression((PsiLiteralExpression) el));

        // 判断是否皆为 PsiLiteralExpression，且字面表达式的值皆不为 null，如果是，直接拼接
        if (Arrays.stream(expressions).allMatch(predicate)) {
            ConstantExpressionEvaluator constantExpressionEvaluator = LanguageConstantExpressionEvaluator.INSTANCE.forLanguage(polyadicExpression.getLanguage());
            return constantExpressionEvaluator.computeConstantExpression(polyadicExpression, true);
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
    private static Object computeReferenceExpression(PsiReferenceExpression referenceExpression) {
        return computeMultiExpression(new PsiExpression[]{referenceExpression});
    }

    /**
     * 计算字面表达式或引用表达式混杂的结果
     *
     * @param expressions 表达式列表
     * @return 结果
     */
    private static Object computeMultiExpression(PsiExpression[] expressions) {
        List<Object> resultList = new ArrayList<>();
        computeMultiExpression(expressions, resultList);
        return combineValues(resultList.toArray());
    }

    /**
     * 递归计算字面表达式或引用表达式混杂的结果
     *
     * @param expressions 表达式列表
     * @param resultList  结果列表
     */
    private static void computeMultiExpression(PsiExpression[] expressions, List<Object> resultList) {
        for (PsiExpression expression : expressions) {
            if (expression instanceof PsiLiteralExpression) {
                resultList.add(PsiUtil.computeLiteralExpression((PsiLiteralExpression) expression));

            } else if (expression instanceof PsiReferenceExpression) {
                PsiReferenceExpression referenceExpression = (PsiReferenceExpression) expression;
                PsiElement resolve = referenceExpression.resolve();

                if (resolve instanceof PsiEnumConstant) {
                    // 枚举类
                    resultList.add(((PsiEnumConstant) resolve).getName());
                } else if (resolve instanceof PsiField) {
                    // 字段
                    resultList.add(computeConstantExpression((PsiField) resolve));
                } else if (resolve instanceof PsiLocalVariable) {
                    // 方法变量
                    resultList.add(computeConstantExpression((PsiLocalVariable) resolve));
                }
            }
        }
    }

    public static Object combineValues(Object... values) {
        // 只要一方为字符串类型，那么整体都将被转为字符串
        if (JsonAssistantUtil.hasStringType(values)) {
            // 字符串
            return concatenateAsString(values);
        } else if (JsonAssistantUtil.allElementsAreNumeric(values)) {
            // 纯数值（包括字符）
            return Arrays.stream(values)
                    // 将每个数值对象转换为double
                    .mapToDouble(el -> el instanceof Number ? ((Number) el).doubleValue() : ((Character) el))
                    // 对所有数值求和
                    .sum();
        }

        return null;
    }

    private static String concatenateAsString(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append(value);
        }
        return sb.toString();
    }

}
