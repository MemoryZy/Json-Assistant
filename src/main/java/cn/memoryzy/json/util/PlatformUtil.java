package cn.memoryzy.json.util;

import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.enums.FileTypes;
import com.intellij.ide.DataManager;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.util.ui.TextTransferable;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class PlatformUtil {
    private static final Logger LOG = Logger.getInstance(PlatformUtil.class);

    /**
     * 获取结构化文件
     *
     * @param dataContext 数据上下文
     * @return 结构化文件
     */
    public static PsiFile getPsiFile(DataContext dataContext) {
        try {
            return dataContext.getData(CommonDataKeys.PSI_FILE);
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
     * 通过当前光标的偏移量获取当前所在的Psi元素
     * <p>亦可配合 PsiTreeUtil.getParentOfType(element, PsiClass.class)方法来获取该PsiElement所处的区域</p>
     *
     * @return Psi元素
     */
    public static PsiElement getPsiElementByOffset(DataContext dataContext) {
        PsiFile psiFile = PlatformUtil.getPsiFile(dataContext);
        Editor editor = PlatformUtil.getEditor(dataContext);
        return (psiFile != null && editor != null) ? getPsiElementByOffset(editor, psiFile) : null;
    }


    /**
     * 获取编辑器
     *
     * @param dataContext 数据上下文
     * @return 编辑器
     */
    public static Editor getEditor(DataContext dataContext) {
        return dataContext.getData(CommonDataKeys.EDITOR);
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

    /**
     * 通过组件获取当前项目
     *
     * @param component 组件
     * @return 项目
     */
    public static @Nullable Project getProject(Component component) {
        DataContext dataContext = DataManager.getInstance().getDataContext(component);
        return CommonDataKeys.PROJECT.getData(dataContext);
    }

    public static FileType getFileType(FileTypes fileTypes) {
        return getFileType(fileTypes, PlainTextFileType.INSTANCE);
    }

    public static FileType getFileType(FileTypes fileTypes, FileType fileType) {
        Class<?> clz = JsonAssistantUtil.getClassByName(fileTypes.getFileTypeQualifiedName());

        if (clz != null) {
            Object instance = JsonAssistantUtil.readStaticFinalFieldValue(clz, fileTypes.getFileTypeInstanceFieldName());
            if (instance instanceof FileType) {
                return (FileType) instance;
            }
        }

        return fileType;
    }

    public static Language getLanguage(FileTypes fileTypes) {
        return getLanguage(fileTypes, PlainTextLanguage.INSTANCE);
    }

    public static Language getLanguage(FileTypes fileTypes, Language language) {
        Class<?> clz = JsonAssistantUtil.getClassByName(fileTypes.getLanguageQualifiedName());

        if (clz != null) {
            Object instance = JsonAssistantUtil.readStaticFinalFieldValue(clz, fileTypes.getLanguageInstanceFieldName());
            if (instance instanceof Language) {
                return (Language) instance;
            }
        }

        return language;
    }


    public static FileType getDocumentFileType(Project project, Document document) {
        if (project == null || null == document) return null;
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        return psiFile == null ? null : psiFile.getFileType();
    }

    public static String computeScratchDirectory() {
        ScratchRootType rootType = ScratchRootType.getInstance();
        ScratchFileService scratchFileService = ScratchFileService.getInstance();
        String scratchRootPath = scratchFileService.getRootPath(rootType);
        String directoryName = JsonAssistantPlugin.PLUGIN_NAME.replace(" ", "-");
        return scratchRootPath + File.separator + directoryName;
    }

    public static String computeScratchProjectDirectory(Project project) {
        ScratchRootType rootType = ScratchRootType.getInstance();
        ScratchFileService scratchFileService = ScratchFileService.getInstance();
        String scratchRootPath = scratchFileService.getRootPath(rootType);
        String directoryName = JsonAssistantPlugin.PLUGIN_NAME.replace(" ", "-");
        String projectName = project.getName().replace(" ", "-");
        return scratchRootPath + File.separator + directoryName + File.separator + projectName;
    }

    public static VirtualFile findFileByPath(String filePath) {
        String path = FileUtil.toSystemIndependentName(filePath);
        return VirtualFileManager.getInstance().findFileByNioPath(Paths.get(path));
    }

    public static void deleteDirectory(Project project, String directoryPath) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            VirtualFile dir = findFileByPath(directoryPath);
            if (dir != null) {
                try {
                    dir.delete(dir);
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
    }

    public static boolean isChineseLocale() {
        Locale locale = Locale.getDefault();
        return Locale.CHINESE.getLanguage().equals(locale.getLanguage())
                && (Objects.equals(locale.getCountry(), "") || Objects.equals(locale.getCountry(), "CN"));
    }
}
