package cn.memoryzy.json.utils;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.util.ui.TextTransferable;

import java.awt.datatransfer.DataFlavor;

/**
 * @author wcp
 * @since 2024/6/20
 */
public class PlatformUtil {

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
