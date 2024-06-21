package cn.memoryzy.json.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.util.ui.TextTransferable;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class PlatformUtil {

    /**
     * 获取编辑器
     *
     * @param event 事件源
     * @return 编辑器
     */
    public static Editor getEditor(AnActionEvent event) {
        return event.getRequiredData(CommonDataKeys.EDITOR);
    }

    /**
     * 获取编辑器中的文本
     *
     * @param event 事件源
     * @return 编辑器文本
     */
    public static String getEditorContent(AnActionEvent event) {
        return getEditor(event).getDocument().getText();
    }



    /**
     * 设置剪贴板内容
     *
     * @param content 要设置到剪贴板中的字符串内容
     */
    public static void setClipboard(String content) {
        CopyPasteManager.getInstance().setContents(new TextTransferable(content));
        // CopyPasteManager.getInstance().setContents(new SimpleTransferable(content, DataFlavor.stringFlavor));
    }

}
