package cn.memoryzy.json.util;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.TextResolveStatus;
import cn.memoryzy.json.model.deserialize.ObjectWrapper;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.data.MessageData;
import cn.memoryzy.json.model.strategy.formats.data.SelectionData;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/10/31
 */
public class TextTransformUtil {

    /**
     * 将处理完成的文本应用至文档内
     *
     * @param project       项目对象
     * @param editor        编辑器对象
     * @param processedText 处理完的文本
     * @param processor     文本处理器
     * @param canWrite      当前文档是否允许写入
     */
    public static void applyProcessedTextToDocument(Project project,
                                                    Editor editor,
                                                    String processedText,
                                                    AbstractGlobalTextConversionProcessor processor,
                                                    boolean canWrite) {
        // 若当前文档允许写入
        if (canWrite) {
            applyTextWhenWritable(project, editor, processedText, processor);
        } else {
            applyTextWhenNotWritable(project, processedText, processor.getFileTypeData().getProcessedFileType());
        }

        removeSelection(processor);
    }

    /**
     * 当文档可写入时，将处理完成的文本应用至文档内
     *
     * @param project       项目对象
     * @param editor        编辑器对象
     * @param processedText 处理完的文本
     * @param processor     文本处理器
     */
    public static void applyTextWhenWritable(Project project, Editor editor, String processedText, AbstractGlobalTextConversionProcessor processor) {
        Document document = editor.getDocument();
        EditorData editorData = processor.getEditorData();
        MessageData messageData = processor.getMessageData();

        // 获取当前文档内的 PsiFile
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        WriteCommandAction.runWriteCommandAction(project, () -> {
            String hintText;

            // 若是选中的文本解析成功
            if (TextResolveStatus.SELECTED_SUCCESS.equals(processor.getTextResolveStatus())) {
                // 获取选中区域相关信息
                SelectionData selectionData = editorData.getSelectionData();
                // 在选中区域中替换处理过后的文本
                document.replaceString(selectionData.getStartOffset(), selectionData.getEndOffset(), processedText);
                // 移动光标至起始位置，便于展示提醒
                editorData.getPrimaryCaret().moveToOffset(selectionData.getStartOffset());
                // 将提醒文本设置为预先定义的文本
                hintText = messageData.getSelectionConvertSuccessMessage();

            } else {
                // 若是文档内的全部文本解析成功
                document.setText(processedText);
                // 是否需要格式化文本
                if (Boolean.TRUE.equals(processor.isNeedBeautify())) {
                    // 格式化
                    Optional.ofNullable(psiFile).ifPresent(el -> CodeStyleManager.getInstance(project).reformatText(psiFile, 0, document.getTextLength()));
                }

                // 移动光标至末尾，便于展示提醒
                editorData.getPrimaryCaret().moveToOffset(0);
                // 将提醒文本设置为预先定义的文本
                hintText = messageData.getGlobalConvertSuccessMessage();
            }

            HintManager.getInstance().showInformationHint(editor, hintText);
        });
    }

    /**
     * 当文档不可写入时，新开工具窗口选项页
     *
     * @param project       项目对象
     * @param processedText 处理完的文本
     * @param fileType      指定新编辑器的文本类型
     */
    public static void applyTextWhenNotWritable(Project project, String processedText, FileType fileType) {
        // 若当前文档不允许写入，则新开工具窗口，用于展示处理完的文本
        try {
            ToolWindowUtil.addNewContentWithEditorContentIfNeeded(project, processedText, fileType);
        } catch (Exception e) {
            copyToClipboardAndShowNotification(project, processedText);
        }
    }

    public static void copyToClipboardAndShowNotification(Project project, String processedText) {
        copyToClipboardAndShowNotification(project, processedText, JsonAssistantBundle.messageOnSystem("tip.no.write.json.copy.text"));
    }

    public static void copyToClipboardAndShowNotification(Project project, String processedText, String message) {
        PlatformUtil.setClipboard(processedText);
        Notifications.showNotification(message, NotificationType.INFORMATION, project);
    }


    /**
     * 去除选中
     *
     * @param processor 处理器
     */
    private static void removeSelection(AbstractGlobalTextConversionProcessor processor) {
        if (TextResolveStatus.SELECTED_SUCCESS.equals(processor.getTextResolveStatus())) {
            processor.getEditorData().getPrimaryCaret().removeSelection();
        }
    }


    /**
     * 判断当前文档是否允许写入
     *
     * @param dataContext                   操作数据上下文
     * @param document                      文档对象
     * @param hasSelection                  当前是否选中文本区域
     * @param allowedFileTypeQualifiedNames 允许写入的文件类型（多）
     * @return 允许写入为 true；反之为 false
     */
    public static boolean canWriteToDocument(DataContext dataContext, Document document, boolean hasSelection, String... allowedFileTypeQualifiedNames) {
        Project project = CommonDataKeys.PROJECT.getData(dataContext);

        // 文档不可写 或 文档属于控制台文档，则不允许写入
        if (!document.isWritable() || isConsoleDocument(dataContext)) {
            return false;
        }

        // 如果选中了文本区域，则允许写入
        if (hasSelection) {
            return true;
        }

        // 获取当前文件类型，若获取不到，默认按可写入处理
        FileType fileType = PlatformUtil.getDocumentFileType(project, document);
        if (fileType == null) {
            return true;
        }

        // 判断当前文件类型是否符合指定的允许条件，若符合，则允许写入
        return Arrays.asList(allowedFileTypeQualifiedNames).contains(fileType.getClass().getName());
    }


    /**
     * 判断当前是否处于控制台文档内
     *
     * @param dataContext 操作数据上下文
     * @return 处于控制台文档内为 true；反之为 false
     */
    public static boolean isConsoleDocument(DataContext dataContext) {
        return null != LangDataKeys.CONSOLE_VIEW.getData(dataContext);
    }


    public static String urlParamsToJson(String url) {
        try {
            // 去掉URL中的协议、主机名等部分，只保留查询字符串
            String query = new java.net.URL(url).getQuery();
            if (query == null || query.isEmpty()) {
                return null;
            }

            // 解析查询字符串
            Map<String, Object> params = new LinkedHashMap<>();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int index = pair.indexOf("=");
                if (index > 0 && index < pair.length() - 1) {
                    String key = URLDecoder.decode(pair.substring(0, index), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair.substring(index + 1), StandardCharsets.UTF_8);

                    // 判断是否为数字、时间、布尔类型
                    params.put(key, JsonAssistantUtil.detectType(value));
                }
            }

            // 转换为JSON
            return new ObjectWrapper(params).toString();
        } catch (Exception e) {
            // LOG.error("Error parsing URL parameters", e);
            return null;
        }
    }


    // public static String keyValuePairsToJson(String keyValue) {
    //     Map<String, String> params = new HashMap<>();
    //     String[] lines = keyValue.split("\\n");
    //
    //     for (String line : lines) {
    //         // 使用正则表达式匹配 ":" 或 "="
    //         String[] parts = line.split("[:=]", 2);
    //         if (parts.length == 2) {
    //             String key = parts[0].trim();
    //             String value = parts[1].trim();
    //
    //             // 判断键是否为数字或布尔值
    //             if (Objects.isNull(getNumber(key)) && Objects.isNull(getBoolean(key))) {
    //                 params.put(key, value);
    //             }
    //         }
    //     }
    //
    //
    // }

}
