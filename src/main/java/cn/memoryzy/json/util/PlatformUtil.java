package cn.memoryzy.json.util;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.action.toolwindow.OpenFromFileAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.Urls;
import cn.memoryzy.json.enums.FileTypes;
import cn.memoryzy.json.model.EditorContext;
import cn.memoryzy.json.model.deserializer.PluginDetail;
import cn.memoryzy.json.model.deserializer.PluginUpdateDetail;
import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.conversion.ComponentManagerSettings;
import com.intellij.conversion.ConversionContext;
import com.intellij.conversion.impl.ConversionContextImpl;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.impl.stores.IProjectStore;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider;
import com.intellij.openapi.fileEditor.impl.text.TextEditorImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.project.ProjectKt;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.util.ResourceUtil;
import com.intellij.util.ui.TextTransferable;
import com.intellij.util.ui.UIUtil;
import icons.JsonAssistantIcons;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

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

    public static PsiFile getPsiFile(Project project, Editor editor) {
        if (null == editor) return null;
        return PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    }

    public static PsiFile getPsiFile(DataContext dataContext, Document document) {
        PsiFile psiFile = getPsiFile(dataContext);
        if (psiFile == null) {
            Project project = CommonDataKeys.PROJECT.getData(dataContext);
            if (project != null) {
                psiFile = getPsiFile(project, document);
            }
        }

        return psiFile;
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
        // StringUtil.convertLineSeparators(text)
        text = JsonAssistantUtil.normalizeLineEndings(text);
        if (text == null) return;
        document.setText(text);
    }

    public static void safeSetDocumentText(Project project, Document document, String text) {
        WriteCommandAction.runWriteCommandAction(project, () -> setDocumentText(document, text));
    }

    public static void openOnlineDoc(Project project, boolean useHtmlEditor) {
        String url = Urls.OVERVIEW;
        boolean darkTheme = UIUtil.isUnderDarcula();
        Map<String, String> parameters = darkTheme ? Map.of("theme", "dark") : Map.of("theme", "light");
        url = com.intellij.util.Urls.newFromEncoded(url).addParameters(parameters).toExternalForm();

        if (PlatformUtil.canBrowseInHTMLEditor() && useHtmlEditor) {
            String timeoutContent = loadText("html", "Timeout.html")
                    .replace("__THEME__", darkTheme ? "theme-dark" : "")
                    .replace("__TITLE__", JsonAssistantBundle.messageOnSystem("open.html.timeout.title"))
                    .replace("__MESSAGE__", JsonAssistantBundle.messageOnSystem("open.html.timeout.message"))
                    .replace("__ACTION__", JsonAssistantBundle.messageOnSystem("open.html.timeout.action", url));

            HTMLEditorProvider.openEditor(project, JsonAssistantBundle.messageOnSystem("open.html.quick.start.title"), url, timeoutContent);
            return;
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

        ConversionContext conversionContext = new ConversionContextImpl(projectBasePath);
        return conversionContext.getProjectRootManagerSettings();
    }

    /**
     * 为指定配置文件创建数据管理
     *
     * @param project  项目
     * @param fileName 文件名
     * @return 数据管理器
     */
    public static ComponentManagerSettings createProjectSettings(Project project, String fileName) {
        IProjectStore store = ProjectKt.getStateStore(project);
        Path projectBasePath = store.getProjectBasePath();

        ConversionContext conversionContext = new ConversionContextImpl(projectBasePath);
        return conversionContext.createProjectSettings(fileName);
    }


    /**
     * 获取IDE的全局配置路径
     *
     * @return 全局配置路径
     */
    public static String getApplicationConfigPath() {
        return PathManager.getOptionsPath();
    }

    /**
     * 获取指定配置文件（XML格式）
     *
     * @param configFileName 配置文件名
     * @return 配置文件
     */
    public static File getOptionsConfigFile(@NotNull String configFileName) {
        return PathManager.getOptionsFile(configFileName);
    }

    /**
     * 从虚拟文件获取内容（带异常处理）
     */
    public static String getContentFromVirtualFile(VirtualFile virtualFile) {
        try {
            // 尝试通过文件文档管理器获取（保留行结束符）
            FileDocumentManager docManager = FileDocumentManager.getInstance();
            Document document = docManager.getDocument(virtualFile);
            if (document != null) {
                return document.getText();
            }

            // 直接加载文件内容
            return new String(virtualFile.contentsToByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 处理所有可能的异常（文件不存在、权限问题等）
            return null;
        }
    }


    public static String getFileRealContent(Project project, VirtualFile file) {
        try {
            PsiFile psiFile = PsiUtil.getPsiFile(project, file);
            String text = psiFile.getText();

            if (StrUtil.isBlank(text)) {
                text = Optional.ofNullable(PsiDocumentManager.getInstance(project).getDocument(psiFile))
                        .map(Document::getText)
                        .orElse(null);
            }

            return text;
        } catch (Exception e) {
            LOG.error("[Json Assistant] Failed to get text", e);
        }

        return null;
    }


    public static Editor createEditor(Project project, String fileName, FileType fileType, boolean isViewer, EditorKind kind, String text) {
        fileName = fileName + "." + fileType.getDefaultExtension();
        VirtualFile sourceVirtualFile = new LightVirtualFile(fileName, fileType, text);
        PsiFile sourceFile = PsiManager.getInstance(project).findFile(sourceVirtualFile);

        assert sourceFile != null;
        Document document = PsiDocumentManager.getInstance(project).getDocument(sourceFile);

        assert document != null;
        return EditorFactory.getInstance().createEditor(document, project, sourceVirtualFile, isViewer, kind);
    }

    public static Editor createEditor(Project project, VirtualFile virtualFile, boolean isViewer, EditorKind kind) {
        PsiFile sourceFile = PsiManager.getInstance(project).findFile(virtualFile);

        assert sourceFile != null;
        Document document = PsiDocumentManager.getInstance(project).getDocument(sourceFile);

        assert document != null;
        return EditorFactory.getInstance().createEditor(document, project, virtualFile, isViewer, kind);
    }

    public static VirtualFile createLightVirtualFile(String fileName, FileType fileType) {
        fileName = fileName + "." + fileType.getDefaultExtension();
        return new LightVirtualFile(fileName, fileType, "");
    }

    public static VirtualFile createLightVirtualFile(String fileName, FileType fileType, String text) {
        fileName = fileName + "." + fileType.getDefaultExtension();
        return new LightVirtualFile(fileName, fileType, text);
    }

    public static String getFullProductName() {
        return ApplicationNamesInfo.getInstance().getFullProductName();
    }

    public static boolean isIdea() {
        return Objects.equals("IntelliJ IDEA", getFullProductName());
    }

    public static EditorEx getEditor(Project project, VirtualFile file) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        FileEditor[] fileEditors = fileEditorManager.getEditors(file);

        for (FileEditor fileEditor : fileEditors) {
            String name = fileEditor.getName();
            EditorEx editorEx = EditorUtil.getEditorEx(fileEditor);
            if (Objects.nonNull(editorEx) && Objects.equals(IdeBundle.message("tab.title.text"), name) && fileEditor instanceof TextEditorImpl) {
                return editorEx;
            }
        }

        return null;
    }


    /**
     * 加载文件文本
     *
     * @param basePath 目录路径（resources目录下）
     * @param fileName 文件名
     * @return 文本
     */
    public static String loadText(String basePath, String fileName) {
        try (InputStream stream = ResourceUtil.getResourceAsStream(JsonAssistantIcons.class.getClassLoader(), basePath, fileName)) {
            return ResourceUtil.loadText(stream);
        } catch (Exception e) {
            LOG.error("[Json Assistant] Failed to load text", e);
        }

        return StrUtil.EMPTY;
    }

    /**
     * 加载字体文件
     *
     * @param basePath 目录路径（resources目录下）
     * @param fontName 文件名
     * @return 字体
     */
    public static Font loadFont(String basePath, String fontName, float size) {
        try (InputStream stream = ResourceUtil.getResourceAsStream(JsonAssistantIcons.class.getClassLoader(), basePath, fontName)) {
            if (null != stream) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
                return font.deriveFont(size);
            } else {
                // throw new RuntimeException("Font file not found in resources.");
                return null;
            }

        } catch (Exception e) {
            LOG.error("[Json Assistant] Failed to load font", e);
        }

        return null;
    }



    public static String getSingleSelectText(Editor editor) {
        if (null == editor) return null;
        Document document = editor.getDocument();
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        int startOffset = primaryCaret.getSelectionStart();
        int endOffset = primaryCaret.getSelectionEnd();
        return document.getText(new TextRange(startOffset, endOffset));
    }

    public static boolean hasJavaEnvironment(Project project) {
        Class<?> languageClz = JsonAssistantUtil.getClassByName(FileTypes.JAVA.getLanguageQualifiedName());
        Class<?> classClz = JsonAssistantUtil.getClassByName("com.intellij.psi.PsiClass");
        return project != null && languageClz != null && classClz != null;
    }

    public static boolean hasJsonEnvironment(Project project) {
        Class<?> fileClz = JsonAssistantUtil.getClassByName("com.intellij.json.psi.JsonFile");
        Class<?> typeClz = JsonAssistantUtil.getClassByName("com.intellij.json.JsonFileType");
        return project != null && fileClz != null && typeClz != null;
    }


    public static PluginDetail getPluginDetail() {
        try {
            String xml = HttpUtil.get(Urls.PLUGIN_DETAILS_LINK, StandardCharsets.UTF_8);
            PluginDetail pluginDetail = XmlUtil.parseXmlString(xml, PluginDetail.class);
            sortPluginsByUpdatedDate(pluginDetail);
            // 更新日志区分为中英文
            resolveMultiLocaleChangeNotes(pluginDetail);
            return pluginDetail;
        } catch (Exception e) {
            return null;
        }
    }

    private static void sortPluginsByUpdatedDate(PluginDetail detail) {
        if (detail != null &&
                detail.getCategory() != null &&
                detail.getCategory().getIdeaPlugins() != null) {

            List<PluginDetail.IdeaPlugin> plugins = detail.getCategory().getIdeaPlugins();
            plugins.sort(Comparator.comparingLong(PluginDetail.IdeaPlugin::getUpdatedDate).reversed());
        }
    }

    private static void resolveMultiLocaleChangeNotes(PluginDetail detail) {
        if (detail != null &&
                detail.getCategory() != null &&
                detail.getCategory().getIdeaPlugins() != null) {

            List<PluginDetail.IdeaPlugin> plugins = detail.getCategory().getIdeaPlugins();

            for (PluginDetail.IdeaPlugin plugin : plugins) {
                String changeNotes = StrUtil.trim(plugin.getChangeNotes());
                // 被cdata包裹的文本，要去除此包裹
                if (changeNotes.startsWith("<![CDATA[") && changeNotes.endsWith("]]>")) {
                    changeNotes = XmlUtil.extractCdataContent(changeNotes);
                }

                ImmutablePair<String, String> pair = Notifications.distinguishChineseAndEnglishChangeNote(changeNotes);
                plugin.setChineseChangeNotes(pair.left);
                plugin.setEnglishChangeNotes(pair.right);
            }
        }
    }

    public static List<PluginUpdateDetail> getPluginUpdateDetail() {
        try {
            String json = HttpUtil.get(Urls.PLUGIN_UPDATE_DETAILS_LINK, StandardCharsets.UTF_8);
            List<PluginUpdateDetail> pluginUpdateDetails = JsonUtil.MAPPER.readValue(json, new TypeReference<>() {
            });
            pluginUpdateDetails.sort(Comparator.comparingLong(PluginUpdateDetail::getCdate).reversed());
            pluginUpdateDetails.forEach(PlatformUtil::resolveMultiLocaleChangeNotes);
            return pluginUpdateDetails;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    private static void resolveMultiLocaleChangeNotes(PluginUpdateDetail detail) {
        ImmutablePair<String, String> pair = Notifications.distinguishChineseAndEnglishChangeNote(detail.getNotes());
        detail.setZhNotes(pair.left);
        detail.setEnNotes(pair.right);
    }


    /**
     * 获取编辑器及文件上下文信息
     *
     * @param project 项目
     * @param editor  编辑器
     * @return 上下文
     */
    public static EditorContext getEditorContext(Project project, Editor editor) {
        EditorContext editorContext = new EditorContext();
        if (null == project || null == editor) return editorContext;

        editorContext.setEditor(editor);
        PsiFile psiFile = getPsiFile(project, editor);
        if (null == psiFile) return editorContext;

        // 如果是内存文件，那 VirtualFile 为空
        return editorContext.setPsiFile(psiFile).setFile(psiFile.getVirtualFile());
    }


    /**
     * 判断当前编辑器是否为新窗口打开的
     *
     * @param project 项目
     * @param file    虚拟文件
     * @return 是否为新窗口打开的
     */
    public static boolean isNewWindow(Project project, VirtualFile file) {
        Window mainWindow = WindowManager.getInstance().getFrame(project);
        Window editorWindow = Optional.ofNullable(FileEditorManager.getInstance(project).getSelectedEditor(file))
                .map(FileEditor::getComponent)
                .map(SwingUtilities::getWindowAncestor)
                .orElse(null);

        return editorWindow != null && !editorWindow.equals(mainWindow);
    }

    public static boolean isJsonFile(PsiFile psiFile) {
        if (psiFile == null) {
            return false;
        }

        Class<?> clazz = psiFile.getClass();
        for (Class<?> iface : clazz.getInterfaces()) {
            if ("com.intellij.json.psi.JsonFile".equals(iface.getName())) {
                return true;
            }
        }
        return false;
    }

    public static void markVirtualFileWritable(VirtualFile file) {
        // 包装文件，允许修改
        try {
            file.setWritable(true);
        } catch (IOException ignored) {
        }

        file.putUserData(OpenFromFileAction.EXTERNAL_FILE_MARKER, true);
    }

    /**
     * 检查指定插件是否已安装并启用
     * @param pluginId 插件ID
     */
    public static boolean isPluginEnabled(String pluginId) {
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId(pluginId));
        return plugin != null && plugin.isEnabled();
    }

    public static boolean isLegacyFloatingToolbarProvider() {
        // 获取所有名为 "register" 的方法
        Method[] registerMethods = ReflectUtil.getMethods(FloatingToolbarProvider.class, method -> "register".equals(method.getName()));
        // 检查是否只有一个 register 方法
        if (registerMethods.length != 1) return false;

        Method registerMethod = registerMethods[0];
        Class<?>[] parameterTypes = registerMethod.getParameterTypes();

        // 检查参数数量和类型
        return parameterTypes.length == 2
                && "com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent".equals(parameterTypes[0].getName())
                && "com.intellij.openapi.Disposable".equals(parameterTypes[1].getName());
    }
}
