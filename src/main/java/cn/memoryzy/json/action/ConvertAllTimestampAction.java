package cn.memoryzy.json.action;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.GlobalTextConverter;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.data.MessageData;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.util.*;
import com.intellij.json.psi.*;
import com.intellij.json.psi.impl.JsonRecursiveElementVisitor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Memory
 * @since 2025/2/25
 */
public class ConvertAllTimestampAction extends DumbAwareAction {

    public ConvertAllTimestampAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.convert.timestamp.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.convert.timestamp.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        DataContext dataContext = e.getDataContext();
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        if (psiFile instanceof JsonFile) {
            handleElement(project, psiFile);
        } else {
            handleWrapper(project, dataContext);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(containsTimestamp(e.getDataContext()));
    }

    public static boolean containsTimestamp(@NotNull DataContext dataContext) {
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        if (psiFile instanceof JsonFile) {
            return containsTimestampInElement(psiFile);

        } else {
            // 文本判断
            return containsTimestampInJson(dataContext);
        }
    }


    // ================================== Element ================================== //

    private void handleElement(Project project, PsiFile psiFile) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            psiFile.accept(new JsonRecursiveElementVisitor() {
                @Override
                public void visitObject(@NotNull JsonObject o) {
                    super.visitObject(o);
                    List<JsonProperty> propertyList = o.getPropertyList();
                    for (JsonProperty property : propertyList) {
                        JsonValue jsonValue = property.getValue();
                        fixAllElement(project, jsonValue);
                    }
                }

                @Override
                public void visitArray(@NotNull JsonArray o) {
                    super.visitArray(o);
                    List<JsonValue> valueList = o.getValueList();
                    for (JsonValue jsonValue : valueList) {
                        fixAllElement(project, jsonValue);
                    }
                }
            });
        });
    }

    // region Element 方法
    private void fixAllElement(Project project, JsonValue jsonValue) {
        // 判断是否为 String、Number 类型
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

        if (timestamp != 0) {
            String time = JsonAssistantUtil.formatDateBasedOnTimestampDetails(timestamp);
            replaceStringElement(project, jsonValue, time);
        }
    }

    private void replaceStringElement(Project project, JsonValue jsonValue, String content) {
        content = "\"" + content + "\"";
        jsonValue.replace(new JsonElementGenerator(project).createValue(content));
    }


    private static boolean containsTimestampInElement(PsiFile psiFile) {
        AtomicInteger atomic = new AtomicInteger(0);
        psiFile.accept(new JsonRecursiveElementVisitor() {
            @Override
            public void visitObject(@NotNull JsonObject o) {
                super.visitObject(o);
                List<JsonProperty> propertyList = o.getPropertyList();
                for (JsonProperty property : propertyList) {
                    JsonValue jsonValue = property.getValue();
                    if (isTimestampValue(jsonValue)) {
                        atomic.incrementAndGet();
                        return;
                    }
                }
            }
        });

        return atomic.get() > 0;
    }

    private static boolean isTimestampValue(JsonValue jsonValue) {
        // 判断是否为 String、Number 类型
        if (jsonValue instanceof JsonStringLiteral) {
            String value = ((JsonStringLiteral) jsonValue).getValue();
            // 若为有效时间戳
            return JsonAssistantUtil.isValidTimestamp(value);

        } else if (jsonValue instanceof JsonNumberLiteral) {
            // double长数值默认表示为科学计数法形式，需转为原样
            double value = ((JsonNumberLiteral) jsonValue).getValue();
            BigDecimal bigDecimalValue = new BigDecimal(value + "");
            long longValue = bigDecimalValue.longValue();

            // 若为有效时间戳
            return JsonAssistantUtil.isValidTimestamp(longValue + "");
        }

        return false;
    }
    // endregion


    // ================================== Text ================================== //

    private void handleWrapper(Project project, DataContext dataContext) {
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        EditorData editorData = GlobalTextConverter.resolveEditor(PlatformUtil.getEditor(dataContext));
        String json = GlobalJsonConverter.parseJson(context, editorData);
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
            processObject((ObjectWrapper) wrapper);
        } else if (wrapper instanceof ArrayWrapper) {
            processArray((ArrayWrapper) wrapper);
        }

        String jsonString;
        if (JsonUtil.isFormattedJson(json)) {
            jsonString = isJson ? JsonUtil.formatJson(wrapper) : Json5Util.formatJson5(wrapper);
        } else {
            jsonString = isJson ? JsonUtil.compressJson(wrapper) : Json5Util.compressJson5(wrapper);
        }

        // 提示信息
        MessageData messageData = processor.getMessageData();
        messageData.setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.convertAllTimestamp"));
        messageData.setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.convertAllTimestamp"));

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

    private void processArray(ArrayWrapper wrapper) {
        for (int i = 0; i < wrapper.size(); i++) {
            Object value = wrapper.get(i);
            if (value instanceof ObjectWrapper) {
                processObject((ObjectWrapper) value);
            } else if (value instanceof ArrayWrapper) {
                processArray((ArrayWrapper) value);
            } else {
                if (isTimestamp(value)) {
                    String time = resolveTimestamp(value);
                    if (StrUtil.isNotBlank(time)) {
                        wrapper.set(i, time);
                    }
                }
            }
        }
    }

    private void processObject(ObjectWrapper wrapper) {
        for (Map.Entry<String, Object> entry : wrapper.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof ObjectWrapper) {
                processObject((ObjectWrapper) value);
            } else if (value instanceof ArrayWrapper) {
                processArray((ArrayWrapper) value);
            } else {
                if (isTimestamp(value)) {
                    String time = resolveTimestamp(value);
                    if (StrUtil.isNotBlank(time)) {
                        entry.setValue(time);
                    }
                }
            }
        }
    }

    private String resolveTimestamp(Object value) {
        long timestamp = 0;
        if (value instanceof String) {
            timestamp = Long.parseLong((String) value);

        } else if (value instanceof Number) {
            timestamp = ((Number) value).longValue();
        }

        return timestamp != 0 ? JsonAssistantUtil.formatDateBasedOnTimestampDetails(timestamp) : null;
    }

    private static boolean containsTimestampInJson(DataContext dataContext) {
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String json = GlobalJsonConverter.parseJson(context, PlatformUtil.getEditor(dataContext));
        JsonWrapper wrapper = GlobalJsonConverter.isValidJson(context.getProcessor()) ? JsonUtil.parse(json) : Json5Util.parse(json);

        if (wrapper instanceof ObjectWrapper) {
            return checkObject((ObjectWrapper) wrapper);
        } else if (wrapper instanceof ArrayWrapper) {
            return checkArray((ArrayWrapper) wrapper);
        }

        return false;
    }

    private static boolean checkObject(ObjectWrapper wrapper) {
        for (Map.Entry<?, ?> entry : wrapper.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof ObjectWrapper) {
                if (checkObject((ObjectWrapper) value)) return true;
            } else if (value instanceof ArrayWrapper) {
                if (checkArray((ArrayWrapper) value)) return true;
            } else {
                if (isTimestamp(value)) return true;
            }
        }

        return false;
    }

    private static boolean checkArray(ArrayWrapper wrapper) {
        for (Object item : wrapper) {
            if (item instanceof ObjectWrapper) {
                if (checkObject((ObjectWrapper) item)) return true;
            } else if (item instanceof ArrayWrapper) {
                if (checkArray((ArrayWrapper) item)) return true;
            } else {
                if (isTimestamp(item)) return true;
            }
        }

        return false;
    }

    private static boolean isTimestamp(Object obj) {
        if (obj instanceof String) {
            return JsonAssistantUtil.isValidTimestamp((String) obj);

        } else if (obj instanceof Number) {
            long l = ((Number) obj).longValue();
            return JsonAssistantUtil.isValidTimestamp(l + "");
        }

        return false;
    }
}
