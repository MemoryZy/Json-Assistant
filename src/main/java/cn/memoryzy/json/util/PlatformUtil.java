package cn.memoryzy.json.util;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.HtmlConstant;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.constant.Urls;
import cn.memoryzy.json.enums.FileTypes;
import com.intellij.conversion.ComponentManagerSettings;
import com.intellij.conversion.impl.ConversionContextImpl;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.DataManager;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.impl.stores.IProjectStore;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.project.ProjectKt;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.util.ui.TextTransferable;
import com.intellij.util.ui.UIUtil;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
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

    public static PsiFile getPsiFile(Project project, Document document) {
        return PsiDocumentManager.getInstance(project).getPsiFile(document);
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

    public static void reformatText(Editor editor) {
        if (Objects.isNull(editor)) {
            return;
        }

        Project project = editor.getProject();
        if (Objects.isNull(project)) {
            return;
        }

        Document document = editor.getDocument();
        PsiFile psiFile = getPsiFile(project, document);

        WriteCommandAction.runWriteCommandAction(
                project,
                () -> CodeStyleManager.getInstance(project).reformatText(psiFile, 0, document.getTextLength()));
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
        boolean isBeNewUi = true;
        try {
            isBeNewUi = Registry.is("ide.experimental.ui", true);
        } catch (Exception e) {
            LOG.warn(e);
        }

        return baselineVersion >= 222 && isBeNewUi;
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

    public static FileType getFileType(FileTypes fileTypes, FileType defaultFileType) {
        Class<?> clz = JsonAssistantUtil.getClassByName(fileTypes.getFileTypeQualifiedName());

        if (clz != null) {
            Object instance = JsonAssistantUtil.readStaticFinalFieldValue(clz, fileTypes.getFileTypeInstanceFieldName());
            if (instance instanceof FileType) {
                return (FileType) instance;
            }
        }

        return defaultFileType;
    }

    public static Language getLanguage(FileTypes fileTypes) {
        return getLanguage(fileTypes, PlainTextLanguage.INSTANCE);
    }

    public static Language getLanguage(FileTypes fileTypes, Language defaultLanguage) {
        Class<?> clz = JsonAssistantUtil.getClassByName(fileTypes.getLanguageQualifiedName());

        if (clz != null) {
            Object instance = JsonAssistantUtil.readStaticFinalFieldValue(clz, fileTypes.getLanguageInstanceFieldName());
            if (instance instanceof Language) {
                return (Language) instance;
            }
        }

        return defaultLanguage;
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


    /**
     * 去除\r相关（{@link com.intellij.openapi.editor.Document} 中不允许带有\r\n的字符，只允许\n）
     *
     * @param text 文本
     */
    public static void setDocumentText(Document document, String text) {
        text = JsonAssistantUtil.normalizeLineEndings(text);
        if (text == null) return;
        document.setText(text);
    }


    public static void openOnlineDoc(Project project, boolean useHtmlEditor) {
        String url = Urls.OVERVIEW;
        boolean darkTheme = UIUtil.isUnderDarcula();
        Map<String, String> parameters = darkTheme ? Map.of("theme", "dark") : Map.of("theme", "light");
        url = com.intellij.util.Urls.newFromEncoded(url).addParameters(parameters).toExternalForm();

        if (PlatformUtil.canBrowseInHTMLEditor() && useHtmlEditor) {
            String timeoutContent = HtmlConstant.TIMEOUT_HTML
                    .replace("__THEME__", darkTheme ? "theme-dark" : "")
                    .replace("__TITLE__", JsonAssistantBundle.messageOnSystem("open.html.timeout.title"))
                    .replace("__MESSAGE__", JsonAssistantBundle.messageOnSystem("open.html.timeout.message"))
                    .replace("__ACTION__", JsonAssistantBundle.messageOnSystem("open.html.timeout.action", url));

            if (Urls.isReachable()) {
                HTMLEditorProvider.openEditor(project, JsonAssistantBundle.messageOnSystem("open.html.quick.start.title"), url, timeoutContent);
                return;
            }
        }

        BrowserUtil.browse(url);
    }

    /**
     * 判断是否是JSON文件类型
     *
     * @param fileType 文件类型
     * @return 是JSON文件类型返回true，否则返回false
     */
    public static boolean isJsonFileType(FileType fileType) {
        return isAssignFileType(fileType, FileTypes.JSON.getFileTypeQualifiedName())
                || isAssignFileType(fileType, FileTypes.JSON5.getFileTypeQualifiedName());
    }

    /**
     * 判断是否是Properties文件类型
     *
     * @param fileType 文件类型
     * @return 是Properties文件类型返回true，否则返回false
     */
    public static boolean isPropertiesFileType(FileType fileType) {
        return isAssignFileType(fileType, FileTypes.PROPERTIES.getFileTypeQualifiedName());
    }

    /**
     * 判断是否是某个文件类型
     *
     * @param fileType          文件类型
     * @param fileTypeClassName 文件类型类名
     * @return 是指定文件类型返回true，否则返回false
     */
    public static boolean isAssignFileType(FileType fileType, String fileTypeClassName) {
        return fileType != null && Objects.equals(fileTypeClassName, fileType.getClass().getName());
    }

    /**
     * 判断是否是JSON语言
     *
     * @param language 语言
     * @return 是JSON语言返回true，否则返回false
     */
    public static boolean isJsonLanguage(Language language) {
        return isAssignLanguage(language, FileTypes.JSON.getLanguageQualifiedName())
                || isAssignLanguage(language, FileTypes.JSON5.getLanguageQualifiedName());
    }

    /**
     * 判断是否是某种语言
     *
     * @param language          语言
     * @param languageClassName 语言类名
     * @return 是指定文件类型返回true，否则返回false
     */
    public static boolean isAssignLanguage(Language language, String languageClassName) {
        return language != null && Objects.equals(languageClassName, language.getClass().getName());
    }

    /**
     * 获取项目数据管理（misc.xml）
     *
     * @param project 项目
     * @return misc.xml文件管理
     */
    public static ComponentManagerSettings getProjectDataManagerSettings(Project project) {
        // String basePath = project.getBasePath();
        IProjectStore store = ProjectKt.getStateStore(project);
        Path projectBasePath = store.getProjectBasePath();

        ConversionContextImpl conversionContext = new ConversionContextImpl(projectBasePath);
        return conversionContext.getProjectRootManagerSettings();
    }

    public static String getFileContent(VirtualFile file) {
        String content = null;
        try {
            content = StrUtil.str(file.contentsToByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Failed to get text", e);
        }

        return content;
    }


    public static Editor createEditor(Project project, String fileName, FileType fileType, boolean isViewer, EditorKind kind, String text) {
        VirtualFile sourceVirtualFile = new LightVirtualFile(fileName, fileType, text);
        PsiFile sourceFile = PsiManager.getInstance(project).findFile(sourceVirtualFile);

        assert sourceFile != null;
        Document document = PsiDocumentManager.getInstance(project).getDocument(sourceFile);

        assert document != null;
        return EditorFactory.getInstance().createEditor(document, project, sourceVirtualFile, isViewer, kind);
    }

    public static String getFullProductName() {
        return ApplicationNamesInfo.getInstance().getFullProductName();
    }

    public static boolean isIdea() {
        return Objects.equals("IntelliJ IDEA", getFullProductName());
    }

}
