package cn.memoryzy.json.util;

import com.intellij.json.psi.*;
import com.intellij.json.psi.impl.JsonRecursiveElementVisitor;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

public class JsonPsiLocator {

    public static Pair<JsonElement, JsonValue> locateByPath(PsiFile psiFile, String jsonPath) {
        if (psiFile instanceof JsonFile) {
            return locateInJsonFile((JsonFile) psiFile, jsonPath);
        }
        // return locateInGenericFile(psiFile, jsonPath);
        return null;
    }

    @Nullable
    private static Pair<JsonElement, JsonValue> locateInJsonFile(JsonFile jsonFile, String path) {
        JsonPathFinder finder = new JsonPathFinder(path);
        jsonFile.accept(finder);
        return finder.getFoundElement();
    }

    @Nullable
    private static PsiElement locateInGenericFile(PsiFile file, String path) {
        // 简化实现：使用文本位置匹配
        PathPosition position = PathParser.parsePathPosition(path);
        return position != null ? findElementAtPosition(file, position) : null;
    }

    private static class JsonPathFinder extends JsonRecursiveElementVisitor {
        private final String targetPath;
        private final Deque<String> currentPath = new ArrayDeque<>();
        private Pair<JsonElement, JsonValue> foundElementPair;

        public JsonPathFinder(String path) {
            this.targetPath = path;
        }

        @Override
        public void visitElement(@NotNull PsiElement element) {
            // 在访问每个元素之前，检查是否已经找到目标
            if (foundElementPair == null) {
                super.visitElement(element);
            }
        }

        @Override
        public void visitProperty(@NotNull JsonProperty property) {
            // 记录当前属性名
            String propertyName = property.getName();
            currentPath.addLast(propertyName);

            String currentPathStr = buildCurrentPath();
            if (targetPath.equals(currentPathStr)) {
                foundElementPair = Pair.pair(property, property.getValue());
                return; // 找到目标，停止遍历
            }

            super.visitProperty(property);

            // 回溯
            currentPath.removeLast();
        }

        @Override
        public void visitArray(@NotNull JsonArray array) {
            // 数组元素没有名称，我们使用索引
            // 在访问数组元素时，我们会记录索引
            super.visitArray(array);
        }

        @Override
        public void visitValue(@NotNull JsonValue value) {
            // 如果当前在数组中，我们需要记录索引
            if (value.getParent() instanceof JsonArray) {
                JsonArray array = (JsonArray) value.getParent();
                int index = array.getValueList().indexOf(value);
                currentPath.addLast("[" + index + "]");

                String currentPathStr = buildCurrentPath();
                if (targetPath.equals(currentPathStr)) {
                    foundElementPair = Pair.pair(array, value);
                    return;
                }

                super.visitValue(value);

                currentPath.removeLast();
            } else {
                super.visitValue(value);
            }
        }

        private String buildCurrentPath() {
            if (currentPath.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder("$");
            for (String component : currentPath) {
                if (component.startsWith("[")) {
                    sb.append(component);
                } else {
                    sb.append('.').append(component);
                }
            }
            return sb.toString();
        }

        public Pair<JsonElement, JsonValue> getFoundElement() {
            return foundElementPair;
        }
    }

    // 路径位置解析器
    private static class PathParser {
        public static PathPosition parsePathPosition(String path) {
            // 简化实现：实际需要完整解析
            return new PathPosition(0, 100); // 示例值
        }
    }

    private static class PathPosition {
        private final int startOffset;
        private final int endOffset;

        public PathPosition(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
    }

    @Nullable
    private static PsiElement findElementAtPosition(PsiFile file, PathPosition position) {
        return file.findElementAt(position.startOffset);
    }
}