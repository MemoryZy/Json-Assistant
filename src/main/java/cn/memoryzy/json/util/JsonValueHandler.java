package cn.memoryzy.json.util;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.JsonValueHandleType;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.GlobalTextConverter;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.data.MessageData;
import cn.memoryzy.json.model.strategy.formats.processor.json.JsonConversionProcessor;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import com.intellij.json.psi.*;
import com.intellij.json.psi.impl.JsonRecursiveElementVisitor;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Memory
 * @since 2025/2/26
 */
public class JsonValueHandler {

    /**
     * 检查当前JSON文件中是否包含特定类型的JSON值（如时间戳或嵌套JSON）。
     *
     * @param dataContext 上下文数据，包含PsiFile和其他相关信息
     * @param handleType  要处理的JSON值类型（例如时间戳、嵌套JSON等）
     * @return 如果找到指定类型的JSON值，则返回true；否则返回false
     */
    public static boolean containsSpecialType(@NotNull DataContext dataContext, JsonValueHandleType handleType) {
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        if (psiFile instanceof JsonFile) {
            return containsSpecialTypeInElement(psiFile, handleType);

        } else {
            // 文本判断
            return containsSpecialTypeInJson(dataContext, handleType);
        }
    }


    // ================================== Element ================================== //

    /**
     * 处理整个JSON元素中所有的指定类型元素
     *
     * @param project    项目
     * @param psiFile    Psi文件
     * @param handleType 指定类型
     */
    public static void handleAllElement(Project project, PsiFile psiFile, JsonValueHandleType handleType) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            psiFile.accept(new JsonRecursiveElementVisitor() {
                @Override
                public void visitObject(@NotNull JsonObject o) {
                    super.visitObject(o);
                    List<JsonProperty> propertyList = o.getPropertyList();
                    for (JsonProperty property : propertyList) {
                        JsonValue jsonValue = property.getValue();
                        handleElement(project, jsonValue, handleType);
                    }
                }

                @Override
                public void visitArray(@NotNull JsonArray o) {
                    super.visitArray(o);
                    List<JsonValue> valueList = o.getValueList();
                    for (JsonValue jsonValue : valueList) {
                        handleElement(project, jsonValue, handleType);
                    }
                }
            });
        });
    }

    // region Element 方法

    /**
     * 根据指定的处理类型（如嵌套JSON或时间戳）处理JSON值。
     *
     * @param project    当前项目对象，用于创建JSON元素生成器
     * @param jsonValue  要处理的JSON值
     * @param handleType 处理类型（例如嵌套JSON或时间戳）
     */
    private static void handleElement(Project project, JsonValue jsonValue, JsonValueHandleType handleType) {
        JsonElementGenerator generator = new JsonElementGenerator(project);
        if (JsonValueHandleType.NESTED_JSON == handleType) {
            if (jsonValue instanceof JsonStringLiteral) {
                String value = ((JsonStringLiteral) jsonValue).getValue();
                if (StrUtil.isNotBlank(value) && (JsonUtil.isJson(value) || Json5Util.isJson5(value))) {
                    String formatted = JsonUtil.isJson(value) ? JsonUtil.formatJson(value) : Json5Util.formatJson5(value);
                    if (StrUtil.isNotBlank(formatted)) {
                        jsonValue.replace(generator.createValue(formatted));
                    }
                }
            }

        } else if (JsonValueHandleType.TIMESTAMP == handleType) {
            // 判断是否为 String、Number 类型
            long timestamp = getValidTimestampInElement(jsonValue);
            if (timestamp != 0) {
                String time = "\"" + JsonAssistantUtil.formatDateBasedOnTimestampDetails(timestamp) + "\"";
                jsonValue.replace(generator.createValue(time));
            }
        }
    }

    /**
     * 在Psi元素中获取可用的时间戳值
     *
     * @param jsonValue JSON值元素
     * @return 时间戳
     */
    private static long getValidTimestampInElement(JsonValue jsonValue) {
        long timestamp = 0;
        if (jsonValue instanceof JsonStringLiteral) {
            String value = ((JsonStringLiteral) jsonValue).getValue();
            // 若为有效时间戳
            if (JsonAssistantUtil.isValidTimestamp(value)) {
                timestamp = Long.parseLong(value);
            }

        } else if (jsonValue instanceof JsonNumberLiteral) {
            // double长数值默认表示为科学计数法形式，需转为原样
            double value = ((JsonNumberLiteral) jsonValue).getValue();
            BigDecimal bigDecimalValue = new BigDecimal(value + "");
            long longValue = bigDecimalValue.longValue();

            // 若为有效时间戳
            if (JsonAssistantUtil.isValidTimestamp(longValue + "")) {
                timestamp = longValue;
            }
        }

        return timestamp;
    }

    /**
     * 在JSON Psi元素是否包含特殊类型
     *
     * @param psiFile    当前文件
     * @param handleType 类型
     * @return 包含为true；否则为false
     */
    private static boolean containsSpecialTypeInElement(PsiFile psiFile, JsonValueHandleType handleType) {
        AtomicInteger atomic = new AtomicInteger(0);
        psiFile.accept(new JsonRecursiveElementVisitor() {
            @Override
            public void visitObject(@NotNull JsonObject o) {
                super.visitObject(o);
                List<JsonProperty> propertyList = o.getPropertyList();
                for (JsonProperty property : propertyList) {
                    JsonValue jsonValue = property.getValue();
                    if (isSpecialTypeValue(jsonValue, handleType)) {
                        atomic.incrementAndGet();
                        return;
                    }
                }
            }

            @Override
            public void visitArray(@NotNull JsonArray o) {
                super.visitArray(o);
                List<JsonValue> valueList = o.getValueList();
                for (JsonValue jsonValue : valueList) {
                    if (isSpecialTypeValue(jsonValue, handleType)) {
                        atomic.incrementAndGet();
                        return;
                    }
                }
            }
        });

        return atomic.get() > 0;
    }

    /**
     * 当前JSON值是否为指定类型
     *
     * @param jsonValue  JSON值
     * @param handleType 指定类型
     * @return 如果是指定类型，则为true；否则为false
     */
    private static boolean isSpecialTypeValue(JsonValue jsonValue, JsonValueHandleType handleType) {
        // 嵌套Json判断
        if (JsonValueHandleType.NESTED_JSON == handleType) {
            if (jsonValue instanceof JsonStringLiteral) {
                String value = ((JsonStringLiteral) jsonValue).getValue();
                // 若为 JSON 格式
                return StrUtil.isNotBlank(value) && (JsonUtil.isJson(value) || Json5Util.isJson5(value));
            }

        } else if (JsonValueHandleType.TIMESTAMP == handleType) {
            // 判断是否为 String、Number 类型
            if (jsonValue instanceof JsonStringLiteral) {
                String value = ((JsonStringLiteral) jsonValue).getValue();
                // 若为有效的时间戳格式
                return JsonAssistantUtil.isValidTimestamp(value);

            } else if (jsonValue instanceof JsonNumberLiteral) {
                // double长数值默认表示为科学计数法形式，需转为原样
                double value = ((JsonNumberLiteral) jsonValue).getValue();
                BigDecimal bigDecimalValue = new BigDecimal(value + "");
                long longValue = bigDecimalValue.longValue();

                // 若为有效时间戳
                return JsonAssistantUtil.isValidTimestamp(longValue + "");
            }
        }

        return false;
    }
    // endregion


    // ================================== Text ================================== //

    /**
     * 处理给定的JSON，根据指定类型进行转换（如嵌套JSON或时间戳）。
     *
     * @param project     当前项目对象
     * @param dataContext 上下文数据，包含编辑器和其他相关信息
     * @param handleType  要处理的JSON值类型（例如嵌套JSON或时间戳）
     */
    public static void handleAllWrapper(Project project, DataContext dataContext, JsonValueHandleType handleType) {
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        // 解析编辑器信息
        EditorData editorData = GlobalTextConverter.resolveEditor(PlatformUtil.getEditor(dataContext));
        // 验证
        if (Objects.isNull(editorData)) return;
        // 获取解析器集合
        JsonConversionProcessor[] processors = GlobalTextConversionProcessorContext.getOriginalAllJsonProcessors(editorData);
        // 解析JSON
        String json = GlobalJsonConverter.parseJson(context, processors);
        // 获取解析成功的处理器
        AbstractGlobalTextConversionProcessor processor = context.getProcessor();
        // 是否为Json格式
        boolean isJson = GlobalJsonConverter.isValidJson(processor);
        // 解析
        JsonWrapper wrapper = isJson ? JsonUtil.parse(json) : Json5Util.parse(json);
        if (wrapper == null) {
            return;
        }

        // 转换
        if (wrapper instanceof ObjectWrapper) {
            processObject((ObjectWrapper) wrapper, handleType);
        } else if (wrapper instanceof ArrayWrapper) {
            processArray((ArrayWrapper) wrapper, handleType);
        }

        String jsonString;
        if (JsonUtil.isFormattedJson(json)) {
            jsonString = isJson ? JsonUtil.formatJson(wrapper) : Json5Util.formatJson5(wrapper);
        } else {
            jsonString = isJson ? JsonUtil.compressJson(wrapper) : Json5Util.compressJson5(wrapper);
        }

        // 提示信息
        MessageData messageData = processor.getMessageData();
        messageData.setSelectionConvertSuccessMessage(handleType.getSelectionConvertSuccessMessage());
        messageData.setGlobalConvertSuccessMessage(handleType.getGlobalConvertSuccessMessage());

        Editor editor = editorData.getEditor();
        FileType fileType = PlatformUtil.getDocumentFileType(project, editor.getDocument());

        boolean hasSelection = processor.getEditorData().getSelectionData().isHasSelection();
        String[] allowedFileTypeQualifiedNames = processor.getFileTypeData().getAllowedFileTypeQualifiedNames();
        // 将当前文件类型添加到白名单
        String[] newAllowedFileTypeQualifiedNames = new String[allowedFileTypeQualifiedNames.length + 1];
        newAllowedFileTypeQualifiedNames[newAllowedFileTypeQualifiedNames.length - 1] = fileType.getClass().getName();
        // 是否可写
        boolean canWrite = TextTransformUtil.canWriteToDocument(dataContext, editor, hasSelection, newAllowedFileTypeQualifiedNames);
        // 处理
        TextTransformUtil.applyProcessedTextToDocument(project, editor, jsonString, processor, canWrite);
    }

    /**
     * 递归处理ArrayWrapper中的值，根据指定类型进行转换（如嵌套JSON或时间戳）。
     *
     * @param wrapper    要处理的ArrayWrapper对象
     * @param handleType 处理类型（NESTED_JSON 或 TIMESTAMP）
     */
    public static void processArray(ArrayWrapper wrapper, JsonValueHandleType handleType) {
        for (int i = 0; i < wrapper.size(); i++) {
            Object value = wrapper.get(i);
            if (value instanceof ObjectWrapper) {
                processObject((ObjectWrapper) value, handleType);
            } else if (value instanceof ArrayWrapper) {
                processArray((ArrayWrapper) value, handleType);
            } else {
                if (isSpecialType(value, handleType)) {
                    Object newValue = resolveSpecialType(value, handleType);
                    if (Objects.nonNull(newValue)) {
                        wrapper.set(i, newValue);
                    }
                }
            }
        }
    }

    /**
     * 递归处理ObjectWrapper中的值，根据指定类型进行转换（如嵌套JSON或时间戳）。
     *
     * @param wrapper    要处理的ObjectWrapper对象
     * @param handleType 处理类型（NESTED_JSON 或 TIMESTAMP）
     */
    public static void processObject(ObjectWrapper wrapper, JsonValueHandleType handleType) {
        for (Map.Entry<String, Object> entry : wrapper.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof ObjectWrapper) {
                processObject((ObjectWrapper) value, handleType);
            } else if (value instanceof ArrayWrapper) {
                processArray((ArrayWrapper) value, handleType);
            } else {
                if (isSpecialType(value, handleType)) {
                    Object newValue = resolveSpecialType(value, handleType);
                    if (Objects.nonNull(newValue)) {
                        entry.setValue(newValue);
                    }
                }
            }
        }
    }

    /**
     * 根据处理类型（嵌套JSON或时间戳）解析并转换给定的值。
     *
     * @param value      要解析的值
     * @param handleType 处理类型（NESTED_JSON 或 TIMESTAMP）
     * @return 解析后的值；如果无法解析，则返回null
     */
    private static Object resolveSpecialType(Object value, JsonValueHandleType handleType) {
        if (JsonValueHandleType.NESTED_JSON == handleType) {
            if (value instanceof String) {
                String json = (String) value;
                return JsonUtil.isJson(json) ? JsonUtil.parse(json) : Json5Util.parse(json);
            }

        } else if (JsonValueHandleType.TIMESTAMP == handleType) {
            long timestamp = 0;
            if (value instanceof String) {
                timestamp = Long.parseLong((String) value);

            } else if (value instanceof Number) {
                timestamp = ((Number) value).longValue();
            }

            return timestamp != 0 ? JsonAssistantUtil.formatDateBasedOnTimestampDetails(timestamp) : null;
        }

        return null;
    }

    /**
     * 检查当前的JSON文本是否包含特定类型的值（如嵌套JSON或时间戳）。
     *
     * @param dataContext 上下文数据，包含编辑器和其他相关信息
     * @param handleType  要检查的JSON值类型（例如嵌套JSON或时间戳）
     * @return 如果找到指定类型的JSON值，则返回true；否则返回false
     */
    private static boolean containsSpecialTypeInJson(DataContext dataContext, JsonValueHandleType handleType) {
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String json = GlobalJsonConverter.parseJson(context, PlatformUtil.getEditor(dataContext));
        if (StrUtil.isBlank(json)) return false;
        JsonWrapper wrapper = GlobalJsonConverter.isValidJson(context.getProcessor()) ? JsonUtil.parse(json) : Json5Util.parse(json);

        if (wrapper instanceof ObjectWrapper) {
            return checkObject((ObjectWrapper) wrapper, handleType);
        } else if (wrapper instanceof ArrayWrapper) {
            return checkArray((ArrayWrapper) wrapper, handleType);
        }

        return false;
    }

    /**
     * 递归检查ObjectWrapper中是否包含特定类型的值（如嵌套JSON或时间戳）。
     *
     * @param wrapper    要检查的ObjectWrapper对象
     * @param handleType 要处理的JSON值类型（例如嵌套JSON或时间戳）
     * @return 如果找到指定类型的值，则返回true；否则返回false
     */
    private static boolean checkObject(ObjectWrapper wrapper, JsonValueHandleType handleType) {
        for (Map.Entry<?, ?> entry : wrapper.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof ObjectWrapper) {
                if (checkObject((ObjectWrapper) value, handleType)) return true;
            } else if (value instanceof ArrayWrapper) {
                if (checkArray((ArrayWrapper) value, handleType)) return true;
            } else {
                if (isSpecialType(value, handleType)) return true;
            }
        }

        return false;
    }

    /**
     * 递归检查ArrayWrapper中是否包含特定类型的值（如嵌套JSON或时间戳）。
     *
     * @param wrapper    要检查的ArrayWrapper对象
     * @param handleType 要处理的JSON值类型（例如嵌套JSON或时间戳）
     * @return 如果找到指定类型的值，则返回true；否则返回false
     */
    private static boolean checkArray(ArrayWrapper wrapper, JsonValueHandleType handleType) {
        for (Object item : wrapper) {
            if (item instanceof ObjectWrapper) {
                if (checkObject((ObjectWrapper) item, handleType)) return true;
            } else if (item instanceof ArrayWrapper) {
                if (checkArray((ArrayWrapper) item, handleType)) return true;
            } else {
                if (isSpecialType(item, handleType)) return true;
            }
        }

        return false;
    }

    /**
     * 检查给定的对象是否为特定类型的值（如嵌套JSON或时间戳）。
     *
     * @param obj        要检查的对象
     * @param handleType 要处理的JSON值类型（例如嵌套JSON或时间戳）
     * @return 如果对象符合指定类型，则返回true；否则返回false
     */
    private static boolean isSpecialType(Object obj, JsonValueHandleType handleType) {
        if (JsonValueHandleType.NESTED_JSON == handleType) {
            if (obj instanceof String) {
                String value = (String) obj;
                return StrUtil.isNotBlank(value) && (JsonUtil.isJson(value) || Json5Util.isJson5(value));
            }

        } else if (JsonValueHandleType.TIMESTAMP == handleType) {
            if (obj instanceof String) {
                return JsonAssistantUtil.isValidTimestamp((String) obj);

            } else if (obj instanceof Number) {
                long l = ((Number) obj).longValue();
                return JsonAssistantUtil.isValidTimestamp(l + "");
            }
        }

        return false;
    }

}
