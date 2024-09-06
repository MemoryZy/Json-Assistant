package cn.memoryzy.json.util;

import com.intellij.psi.PsiLiteralExpression;

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
        return literalExpression.getValue();
    }


}
