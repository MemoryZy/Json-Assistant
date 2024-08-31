package cn.memoryzy.json.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.util.ui.TextTransferable;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class PlatformUtil {

    /**
     * 获取结构化文件
     *
     * @param event 事件源
     * @return 结构化文件
     */
    public static PsiFile getPsiFile(AnActionEvent event) {
        try {
            return event.getData(CommonDataKeys.PSI_FILE);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 通过当前光标的偏移量获取当前所在的Psi元素
     * <p>亦可配合 PsiTreeUtil.getParentOfType(element, PsiClass.class)方法来获取该PsiElement所处的区域</p>
     *
     * @param editor  编辑器
     * @param psiFile Psi文件
     * @return Psi元素
     */
    public static PsiElement getPsiElementByOffset(Editor editor, PsiFile psiFile) {
        return psiFile.findElementAt(editor.getCaretModel().getOffset());
    }


    /**
     * 获取编辑器
     *
     * @param event 事件源
     * @return 编辑器
     */
    public static Editor getEditor(AnActionEvent event) {
        return event.getData(CommonDataKeys.EDITOR);
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

    public static String getClipboard() {
        try {
            Transferable contents = CopyPasteManager.getInstance().getContents();
            if (Objects.isNull(contents)) {
                return "";
            }

            return (String) contents.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * 刷新文件系统
     */
    public static void refreshFileSystem() {
        VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
    }


    public static boolean isNewUi() {
        int baselineVersion = ApplicationInfo.getInstance().getBuild().getBaselineVersion();
        return baselineVersion >= 222 && Registry.is("ide.experimental.ui", true);
    }

    /**
     * 部分平台没有内嵌 Chromium，例如 DataGrip
     *
     * @return 是否支持打开HTMLEditor
     */
    public static boolean canBrowseInHTMLEditor() {
        return JBCefApp.isSupported();
    }


    public static JComponent getMainComponentWithOpenToolWindow(ToolWindow toolWindow) {
        if (Objects.nonNull(toolWindow)) {
            ContentManager contentManager = toolWindow.getContentManager();
            Content content = contentManager.getContent(0);
            if (Objects.nonNull(content)) {
                return content.getComponent();
            }
        }

        return null;
    }

    public static FileType getDocumentFileType(Project project, Document document) {
        if (project == null || null == document) return null;
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        return psiFile == null ? null : psiFile.getFileType();
    }

}
