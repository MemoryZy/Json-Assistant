package cn.memoryzy.json.util;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Json 文本（编辑器场景）光标节点的提取工具
 * <p>
 * 与结构树不同，编辑器场景没有 JsonNode 这样的包装对象，只能借助 IntelliJ 的 Json PSI 定位：
 * 用 {@link PlatformUtil#getPsiElementByOffset(DataContext)} 拿到光标处的 PsiElement，
 * 再向上寻找 {@link JsonValue} / {@link JsonProperty} 父节点，从而得到路径、键与值。
 *
 * @author Memory
 * @since 2025/11/21
 */
public class JsonExtractUtil {

    /**
     * 路径根节点，与结构树的 JsonNode 路径保持一致（如：$.data.list[0].name）
     */
    public static final String ROOT_PATH = "$";

    private JsonExtractUtil() {
    }

    /**
     * 编辑器是否以 JSON / JSON5 格式打开
     * <p>
     * 只以「文件类型」判断，不校验内容是否合法：即便当前 Json 存在语法错误，
     * 只要文件类型是 JSON，提取功能依然可用（PSI 不完整时具体动作会自行隐藏）。
     *
     * @param dataContext 数据上下文
     * @return 是 Json 编辑器返回 true
     */
    public static boolean isJsonEditor(@Nullable DataContext dataContext) {
        if (dataContext == null) {
            return false;
        }

        Editor editor = PlatformUtil.getEditor(dataContext);
        if (editor == null) {
            return false;
        }

        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            project = editor.getProject();
        }
        if (project == null) {
            return false;
        }

        return PlatformUtil.isJsonFileType(PlatformUtil.getDocumentFileType(project, editor.getDocument()));
    }

    /**
     * 获取光标处的 Psi 元素
     * <p>
     * 存在选区时，取「选区最后一个字符」所在的元素：
     * 例如选中某个 key 时，光标通常停在 key 之后（冒号或空白处），
     * 直接按光标取会落到属性外，导致无法识别出所选的键与值。
     *
     * @param project     项目
     * @param dataContext 数据上下文
     * @return Psi 元素，光标不在编辑器内时返回 null
     */
    @Nullable
    public static PsiElement getCaretElement(@Nullable Project project, @Nullable DataContext dataContext) {
        if (dataContext == null) {
            return null;
        }

        PsiElement element = PlatformUtil.getPsiElementByOffset(project, dataContext);
        Editor editor = PlatformUtil.getEditor(dataContext);
        if (element == null || editor == null) {
            return element;
        }

        // 有选区：改用选区末端前一个字符，保证落点在选中的文本内
        if (editor.getSelectionModel().hasSelection()) {
            PsiFile file = element.getContainingFile();
            int offset = editor.getSelectionModel().getSelectionEnd() - 1;
            if (file != null && offset >= 0 && offset < file.getTextLength()) {
                PsiElement selected = file.findElementAt(offset);
                if (selected != null) {
                    return selected;
                }
            }
        }

        return element;
    }

    /**
     * 光标处是否为无意义的空白 / 注释节点
     * <p>
     * 这类位置不属于任何具体的键值，若放任向上查找，会得到外层的对象或数组，
     * 导致「复制值」变成复制整段 Json，因此统一视为不可提取。
     *
     * @param element 光标处的 Psi 元素
     * @return 空白、注释返回 true
     */
    public static boolean isBlankToken(@Nullable PsiElement element) {
        return element instanceof PsiWhiteSpace || element instanceof PsiComment;
    }

    /**
     * 获取光标所在的 Json 属性（键值对）
     * <p>
     * 光标停在键、冒号或值上时，都会返回该属性；
     * 光标停在数组元素、根对象空白处时，返回 null。
     *
     * @param element 光标处的 Psi 元素
     * @return Json 属性，不存在返回 null
     */
    @Nullable
    public static JsonProperty findCaretProperty(@Nullable PsiElement element) {
        return element == null ? null : PsiTreeUtil.getParentOfType(element, JsonProperty.class);
    }

    /**
     * 光标处是否存在可提取的 Json 节点
     * <p>
     * 提取分组下四个动作共用这一判定，保证它们「要么都显示、要么都不显示」：
     * 只要光标落在某个 Json 值（或其键、冒号、值内部的空白）上就算可提取，
     * 光标停在属性之间的空白 / 注释上则不可提取。
     *
     * @param project     项目
     * @param dataContext 数据上下文
     * @return 可提取返回 true
     */
    public static boolean isExtractable(@Nullable Project project, @Nullable DataContext dataContext) {
        if (dataContext == null) {
            return false;
        }

        return resolveCaretValue(getCaretElement(project, dataContext)) != null;
    }

    /**
     * 获取光标 / 选区所在的「值」
     * <p>
     * 光标停在键名、冒号（或选中了某个键）时，返回该键对应的值，
     * 这样「复制值」在键上也能出现并复制到正确的值，而不是外层对象；
     * 光标停在对象的括号、数组元素、字面量上时，返回最内层的 JsonValue；
     * 停在属性之外的空白 / 注释上时返回 null。
     *
     * @param element 光标处的 Psi 元素
     * @return 光标处的 Json 值，不存在返回 null
     */
    @Nullable
    public static JsonValue findCaretValue(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }

        JsonProperty property = PsiTreeUtil.getParentOfType(element, JsonProperty.class);
        if (property != null) {
            JsonValue propertyValue = property.getValue();
            if (propertyValue == null) {
                return null;
            }

            // 光标就在属性值内部：继续取最内层的值
            if (PsiTreeUtil.isAncestor(propertyValue, element, false)) {
                return PsiTreeUtil.getParentOfType(element, JsonValue.class, false);
            }

            // 光标在键名、冒号、键后空白处：视为取该键对应的值
            return propertyValue;
        }

        if (isBlankToken(element)) {
            return null;
        }

        return PsiTreeUtil.getParentOfType(element, JsonValue.class, false);
    }

    /**
     * 将光标处的 Psi 元素解析为用于生成路径的 Json 值
     * <p>
     * 与 {@link #findCaretValue(PsiElement)} 的区别：光标停在键名上时，
     * 这里会返回该键对应的值，从而可以生成「该键所在节点」的路径。
     *
     * @param element 光标处的 Psi 元素
     * @return 用于生成路径的 Json 值，不存在返回 null
     */
    @Nullable
    public static JsonValue resolveCaretValue(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }

        JsonProperty property = PsiTreeUtil.getParentOfType(element, JsonProperty.class);
        JsonValue nearestValue = PsiTreeUtil.getParentOfType(element, JsonValue.class, false);

        // 光标停在键名、冒号上：此时最近的值是外层对象，应改为取当前属性的值
        if (property != null && property.getValue() != null) {
            if (nearestValue == null || !PsiTreeUtil.isAncestor(property.getValue(), element, false)) {
                return property.getValue();
            }
        }

        if (isBlankToken(element)) {
            return null;
        }

        return nearestValue;
    }

    /**
     * 构建 Json 值的路径（形如 $.data.list[0].name）
     * <p>
     * 与结构树 JsonFilterableTree 的 buildCurrentPath / buildArrayElementPath 规则一致：
     * 对象属性统一使用「.key」，数组元素使用「[index]」，根节点为「$」。
     *
     * @param value Json 值
     * @return 路径，无法构建时返回 null
     */
    @Nullable
    public static String buildPath(@Nullable JsonValue value) {
        if (value == null) {
            return null;
        }

        List<String> segments = new ArrayList<>();
        PsiElement current = value;
        while (current != null) {
            PsiElement parent = current.getParent();
            // 已到达文件根部，路径结束
            if (parent == null || parent instanceof JsonFile) {
                break;
            }

            if (parent instanceof JsonProperty) {
                // 当前值属于某个属性：追加「.key」
                String name = ((JsonProperty) parent).getName();
                if (name == null) {
                    return null;
                }
                segments.add(0, "." + name);
                current = parent;

            } else if (parent instanceof JsonArray) {
                // 当前值是数组元素：追加「[index]」
                int index = ((JsonArray) parent).getValueList().indexOf(current);
                if (index < 0) {
                    return null;
                }
                segments.add(0, "[" + index + "]");
                current = parent;

            } else {
                // 对象、注释等中间节点：继续向上
                current = parent;
            }
        }

        return ROOT_PATH + String.join("", segments);
    }

    /**
     * 获取 Json 值的文本
     * <p>
     * 字符串取去掉引号后的内容（与结构树的「复制值」保持一致），
     * 其余类型（数字、布尔、null、对象、数组）直接取原文。
     *
     * @param value Json 值
     * @return 值的文本，不存在返回 null
     */
    @Nullable
    public static String getValueText(@Nullable JsonValue value) {
        if (value == null) {
            return null;
        }

        if (value instanceof JsonStringLiteral) {
            return ((JsonStringLiteral) value).getValue();
        }

        return value.getText();
    }

}