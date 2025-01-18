package cn.memoryzy.json.ui.dialog;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.deserializer.OptionsGroup;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.DependencyConstant;
import cn.memoryzy.json.constant.LanguageHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.JsonAnnotations;
import cn.memoryzy.json.enums.LombokAnnotations;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import cn.memoryzy.json.ui.decorator.TextEditorErrorPopupDecorator;
import cn.memoryzy.json.ui.editor.CustomizedLanguageTextEditor;
import cn.memoryzy.json.util.UIManager;
import cn.memoryzy.json.util.*;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightClassUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author Memory
 * @since 2024/8/23
 */
public class JsonToJavaBeanDialog extends DialogWrapper {
    private static final Logger LOG = Logger.getInstance(JsonToJavaBeanDialog.class);
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(JsonToJavaBeanDialog.class);

    private JBTextField classNameTextField;
    private EditorTextField jsonTextField;
    private TextEditorErrorPopupDecorator classNameErrorDecorator;
    private TextEditorErrorPopupDecorator jsonErrorDecorator;

    private final Project project;
    private final PsiDirectory directory;
    private final Module module;

    private final DeserializerState deserializerState;

    public JsonToJavaBeanDialog(@Nullable Project project, PsiDirectory directory, Module module) {
        super(project, true);
        this.project = project;
        this.directory = directory;
        this.module = module;
        this.deserializerState = JsonAssistantPersistentState.getInstance().deserializerState;

        setTitle(JsonAssistantBundle.messageOnSystem("dialog.deserialize.title"));
        setOKButtonText(JsonAssistantBundle.messageOnSystem("dialog.deserialize.ok"));
        setCancelButtonText(JsonAssistantBundle.messageOnSystem("dialog.deserialize.cancel"));
        getOKAction().setEnabled(false);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        classNameTextField = new JBTextField();
        JBLabel label = new JBLabel(JsonAssistantBundle.messageOnSystem("dialog.deserialize.label.class.name"));
        JPanel firstPanel = SwingHelper.newHorizontalPanel(Component.CENTER_ALIGNMENT, label, classNameTextField);
        firstPanel.setBorder(JBUI.Borders.emptyLeft(4));

        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.X_AXIS)); // 水平排列
        optionPanel.setBorder(JBUI.Borders.empty(5, 0, 0, 4));
        optionPanel.add(new TitledSeparator());
        optionPanel.add(Box.createRigidArea(new Dimension(3, 0)));
        optionPanel.add(createOptionsButton());

        BorderLayoutPanel borderLayoutPanel = new BorderLayoutPanel().addToTop(firstPanel).addToCenter(optionPanel);

        jsonTextField = new CustomizedLanguageTextEditor(LanguageHolder.JSON5, project, "", true);
        jsonTextField.setFont(UIManager.consolasFont(15));
        jsonTextField.setPlaceholder(JsonAssistantBundle.messageOnSystem("dialog.deserialize.placeholder.text") + PluginConstant.JSON_EXAMPLE);
        jsonTextField.setShowPlaceholderWhenFocused(true);
        jsonTextField.addDocumentListener(new JsonValidatorDocumentListener());
        jsonTextField.addNotify();

        classNameErrorDecorator = new TextEditorErrorPopupDecorator(getRootPane(), classNameTextField);
        jsonErrorDecorator = new TextEditorErrorPopupDecorator(getRootPane(), jsonTextField);

        JBSplitter splitter = new JBSplitter(true, 0.06f);
        splitter.setFirstComponent(borderLayoutPanel);
        splitter.setSecondComponent(jsonTextField);
        splitter.setResizeEnabled(false);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(splitter, BorderLayout.CENTER);
        rootPanel.setPreferredSize(new Dimension(480, 450));
        return rootPanel;
    }

    private JComponent createOptionsButton() {
        OptionsGroup group = new OptionsGroup(deserializerState, module);
        return new ActionButton(group, group.getTemplatePresentation(), ActionPlaces.POPUP, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);

        // OptionsGroup2 group = new OptionsGroup2(deserializerState, module);
        //
        // DefaultActionGroup defaultActionGroup = new DefaultActionGroup();
        // defaultActionGroup.add(group);
        //
        // ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, defaultActionGroup, true);
        //
        //
        // // return new ActionButton(group, group.getTemplatePresentation(), ActionPlaces.POPUP, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);
        //
        // return toolbar.getComponent();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return classNameTextField;
    }

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return UrlType.SITE_TO_JAVA_BEAN.getId();
    }

    @Override
    public void show() {
        ApplicationManager.getApplication().invokeLater(super::show);
    }

    @Override
    protected void doOKAction() {
        if (getOKAction().isEnabled()) {
            // 执行逻辑
            if (executeOkAction()) {
                close(OK_EXIT_CODE);
            }
        }
    }

    private boolean executeOkAction() {
        // 获取 Json 文本
        String jsonText = StrUtil.trim(jsonTextField.getText());
        if (StringUtils.isBlank(jsonText)) {
            // 为空则提示
            jsonErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.invalid.json"));
            return false;
        }

        // 获取 ClassName
        String className = classNameTextField.getText();
        if (StringUtils.isBlank(className)) {
            className = "FromJson" + StrUtil.upperFirst(IdUtil.simpleUUID().substring(0, 4));
        } else {
            // 存在名字，校验
            ClassValidator classValidator = new ClassValidator(project, directory);
            if (!(classValidator.checkInput(className) && classValidator.canClose(className))) {
                classNameErrorDecorator.setError(classValidator.getErrorText(className));
                return false;
            }
        }

        // 判断文件是否已经存在
        if (directory.findFile(className + ".java") != null) {
            // 提示
            classNameErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.already.exists", className));
            return false;
        }

        // 解析Json
        ObjectWrapper jsonObject = resolveJson(jsonText);

        // 属性为空
        if (Objects.isNull(jsonObject) || jsonObject.isEmpty()) {
            jsonErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.invalid.json"));
            return false;
        }

        // 先生成Java文件
        JavaDirectoryService directoryService = JavaDirectoryService.getInstance();
        PsiClass newClass = directoryService.createClass(directory, className);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                Set<String> needImportList = new HashSet<>();
                // Java元素构建器
                PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
                // 递归添加Json字段
                recursionAddProperty(jsonObject, newClass, factory, needImportList);
                // 添加注解
                addClassLevelAnnotation(newClass, factory);
                // 导入
                JavaUtil.importClassesInClass(project, newClass, needImportList.toArray(new String[0]));

                // 刷新文件系统
                PlatformUtil.refreshFileSystem();

                // 格式化
                CodeStyleManager.getInstance(project).reformat(newClass);

                // 为每个字段添加换行
                addWhiteSpaceToField(newClass);

                // 编辑器定位到新建类
                newClass.navigate(true);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });

        return true;
    }


    // -------------------------------------- Psi 操作 -------------------------------------- //


    private void recursionAddProperty(ObjectWrapper jsonObject, PsiClass psiClass, PsiElementFactory factory, Set<String> needImportList) {
        // 循环所有Json字段
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // 处理后的key
            String processedKey = getFieldName(key);

            // ------------- 如果value是ObjectWrapper，表示是对象
            if (value instanceof ObjectWrapper) {
                ObjectWrapper childJsonObject = (ObjectWrapper) value;
                // 如果是对象，则还需要创建内部类
                PsiClass innerClass = factory.createClass(StrUtil.upperFirst(processedKey));
                // 添加 static 关键字
                JavaUtil.addKeywordsToClass(factory, PsiModifier.STATIC, innerClass);
                // 则递归添加
                recursionAddProperty(childJsonObject, innerClass, factory, needImportList);
                // 添加内部类至主类
                psiClass.add(innerClass);
                // 添加当前内部类类型的字段
                String fieldText = StrUtil.format("{} {} {};", PsiModifier.PRIVATE, innerClass.getName(), processedKey);
                // 构建字段对象
                PsiField psiField = factory.createFieldFromText(fieldText, psiClass);
                // 添加注解
                addFieldLevelAnnotation(key, psiClass, psiField, factory);
                // 添加到Class
                psiClass.add(psiField);

            } else if (value instanceof ArrayWrapper) {
                ArrayWrapper jsonArray = (ArrayWrapper) value;
                if (CollUtil.isNotEmpty(jsonArray)) {
                    String innerClassName;
                    Object element = jsonArray.get(0);
                    if (element instanceof ObjectWrapper) {
                        ObjectWrapper jsonObj = (ObjectWrapper) element;
                        // 对象值
                        innerClassName = StrUtil.upperFirst(processedKey + "Bean");
                        // 如果是对象，则还需要创建内部类
                        PsiClass innerClass = factory.createClass(innerClassName);
                        // 添加 static 关键字
                        JavaUtil.addKeywordsToClass(factory, PsiModifier.STATIC, innerClass);
                        // 则递归添加
                        recursionAddProperty(jsonObj, innerClass, factory, needImportList);
                        // 添加内部类至主类
                        psiClass.add(innerClass);
                    } else {
                        // 普通值
                        innerClassName = (Objects.nonNull(element)) ? element.getClass().getSimpleName() : Object.class.getSimpleName();
                    }

                    needImportList.add(List.class.getName());
                    // 添加当前内部类类型的字段
                    String fieldText = StrUtil.format("{} List<{}> {};", PsiModifier.PRIVATE, innerClassName, processedKey);
                    // 构建字段对象
                    PsiField psiField = factory.createFieldFromText(fieldText, psiClass);
                    // 添加注解
                    addFieldLevelAnnotation(key, psiClass, psiField, factory);
                    // 添加到Class
                    psiClass.add(psiField);

                }
            } else {
                // ------------- 非对象，则直接添加字段
                // 获取字段类型
                String propertyType = JavaUtil.getStrType(value);
                // 定义字段文本
                String fieldText = StrUtil.format("{} {} {};", PsiModifier.PRIVATE, propertyType, processedKey);

                if (Objects.equals(propertyType, Date.class.getSimpleName())) {
                    needImportList.add(Date.class.getName());
                } else if (propertyType.startsWith(List.class.getSimpleName())) {
                    needImportList.add(List.class.getName());
                }

                // 构建字段对象
                PsiField psiField;
                try {
                    psiField = factory.createFieldFromText(fieldText, psiClass);
                } catch (IncorrectOperationException e) {
                    Notifications.showLogNotification(JsonAssistantBundle.messageOnSystem("error.deserialize.incorrect.field", key), NotificationType.ERROR, project);
                    throw e;
                }

                // 添加注解
                addFieldLevelAnnotation(key, psiClass, psiField, factory);

                // 添加到Class
                psiClass.add(psiField);
            }
        }
    }

    private void addFieldLevelAnnotation(String originalKey, PsiClass newClass, PsiField psiField, PsiElementFactory factory) {
        // 是否存在FastJson依赖
        boolean hasFastJsonLib = JavaUtil.hasFastJsonLib(module);
        boolean hasFastJson2Lib = JavaUtil.hasFastJson2Lib(module);
        // 当选择了添加 fastJson/fastJson2 注解，且存在 fastJson/fastJson2 注解
        if ((deserializerState.fastJsonAnnotation || deserializerState.fastJson2Annotation) && (hasFastJsonLib || hasFastJson2Lib)) {
            addFieldLevelFastJsonAnnotation(originalKey, newClass, psiField, factory, hasFastJsonLib, hasFastJson2Lib);
        }

        // 是否存在Jackson依赖
        if (deserializerState.jacksonAnnotation && JavaUtil.hasJacksonLib(module)) {
            addFieldLevelJacksonAnnotation(originalKey, newClass, psiField, factory);
        }
    }

    private void addFieldLevelJacksonAnnotation(String originalKey, PsiClass newClass, PsiField psiField, PsiElementFactory factory) {
        addFieldLevelJsonAnnotation(JsonAnnotations.JACKSON_JSON_PROPERTY.getValue(), "value", originalKey, newClass, psiField, factory);
    }

    private void addFieldLevelFastJsonAnnotation(String originalKey, PsiClass newClass, PsiField psiField,
                                                 PsiElementFactory factory, boolean hasFastJsonLib, boolean hasFastJson2Lib) {
        String annotationName = null;
        if (deserializerState.fastJsonAnnotation && hasFastJsonLib) {
            // 如果选择添加 fastJson 注解，且存在 fastJson 库
            annotationName = JsonAnnotations.FAST_JSON_JSON_FIELD.getValue();
        } else if (deserializerState.fastJson2Annotation && hasFastJson2Lib) {
            // 如果选择添加 fastJson2 注解，且存在 fastJson2 库
            annotationName = JsonAnnotations.FAST_JSON2_JSON_FIELD.getValue();
        }

        if (annotationName == null) {
            return;
        }

        addFieldLevelJsonAnnotation(annotationName, "name", originalKey, newClass, psiField, factory);
    }


    private void addFieldLevelJsonAnnotation(String annotationName, String attributeName, String originalKey,
                                             PsiClass newClass, PsiField psiField, PsiElementFactory factory) {
        // 增加注解
        String formatted = StrUtil.format("@{}({} = \"{}\")", StringUtil.getShortName(annotationName), attributeName, originalKey);
        // 导入类
        JavaUtil.importClassesInClass(project, newClass, annotationName);

        PsiElement firstChild = psiField.getFirstChild();
        PsiAnnotation jsonFieldAnnotation = factory.createAnnotationFromText(formatted, psiField);
        psiField.addBefore(jsonFieldAnnotation, firstChild);
    }


    private void addClassLevelAnnotation(PsiClass newClass, PsiElementFactory factory) {
        // 判断是否存在lombok依赖
        if (JavaUtil.hasLibrary(module, DependencyConstant.LOMBOK_LIB)) {
            // 在类上添加lombok注解，递归给内部类也加上
            addClassLevelLombokAnnotation(project, newClass, factory);
        }
    }

    /**
     * 导入类并添加注解
     *
     * @param project  当前项目
     * @param newClass 待导入的类
     * @param factory  用于创建新的psi元素的工厂
     */
    private void addClassLevelLombokAnnotation(Project project, PsiClass newClass, PsiElementFactory factory) {
        List<PsiAnnotation> annotations = new ArrayList<>();
        List<String> importQualifiedNames = new ArrayList<>();

        // 增加注解
        if (deserializerState.dataLombokAnnotation) {
            annotations.add(factory.createAnnotationFromText("@" + StringUtil.getShortName(LombokAnnotations.DATA.getValue()), null));
            importQualifiedNames.add(LombokAnnotations.DATA.getValue());
        }

        if (deserializerState.accessorsChainLombokAnnotation) {
            annotations.add(factory.createAnnotationFromText("@" + StringUtil.getShortName(LombokAnnotations.ACCESSORS.getValue()) + "(chain = true)", null));
            importQualifiedNames.add(LombokAnnotations.ACCESSORS.getValue());
        }

        if (deserializerState.setterLombokAnnotation) {
            annotations.add(factory.createAnnotationFromText("@" + StringUtil.getShortName(LombokAnnotations.SETTER.getValue()), null));
            importQualifiedNames.add(LombokAnnotations.SETTER.getValue());
        }

        if (deserializerState.getterLombokAnnotation) {
            annotations.add(factory.createAnnotationFromText("@" + StringUtil.getShortName(LombokAnnotations.GETTER.getValue()), null));
            importQualifiedNames.add(LombokAnnotations.GETTER.getValue());
        }

        // 导入类
        JavaUtil.importClassesInClass(project, newClass, importQualifiedNames.toArray(new String[0]));

        PsiElement firstChild = newClass.getFirstChild();
        if (firstChild instanceof PsiDocComment) {
            for (PsiAnnotation annotation : annotations) {
                newClass.addAfter(annotation, firstChild);
            }
        } else {
            for (PsiAnnotation annotation : annotations) {
                newClass.addBefore(annotation, firstChild);
            }
        }

        // 内部类也加上（要递归）
        this.addImportAnnotationOnInnerClass(newClass, annotations.toArray(new PsiAnnotation[0]));
    }

    private void addImportAnnotationOnInnerClass(PsiClass newClass, PsiAnnotation... classAnnotations) {
        PsiClass[] innerClasses = newClass.getInnerClasses();
        if (ArrayUtil.isNotEmpty(innerClasses)) {
            for (PsiClass innerClass : innerClasses) {
                PsiElement innerFirstChild = innerClass.getFirstChild();
                for (PsiAnnotation classAnnotation : classAnnotations) {
                    innerClass.addBefore(classAnnotation, innerFirstChild);
                }
                // 找内部类中是否还有内部类，并添加上注解
                addImportAnnotationOnInnerClass(innerClass, classAnnotations);
            }
        }
    }


    // ----------------------------------------------------------------------------------- //

    private void addWhiteSpaceToField(PsiClass newClass) {
        PsiFile psiFile = newClass.getContainingFile();
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        Document document = documentManager.getDocument(psiFile);
        if (document == null) {
            return;
        }

        String text = document.getText();
        String[] textArray = text.split("\n");
        List<String> resultList = new ArrayList<>();
        for (String textStr : textArray) {
            // 不包含 package 及 import，且以;结尾，表示字段行
            if ((textStr.endsWith(";") && !textStr.contains("package ") && !textStr.contains("import "))
                    // 如果是class声明行，也加个换行
                    || (textStr.contains("public class ") || textStr.contains("public static class "))) {
                textStr += "\n";
            }

            resultList.add(textStr);
        }

        WriteAction.run(() -> {
            String result = StrUtil.join("\n", resultList);
            // 解锁 Document，防止 Psi 锁住无法修改
            documentManager.doPostponedOperationsAndUnblockDocument(document);
            document.setText(result);
        });
    }

    private ObjectWrapper resolveJson(String jsonText) {
        // 解析Json及Json5
        if (!JsonUtil.isJson(jsonText) && !Json5Util.isJson5(jsonText)) {
            jsonErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.invalid.json"));
            return null;
        }

        // ----------------- 普通Json解析
        if (JsonUtil.isJsonArray(jsonText)) {
            ArrayWrapper jsonArray = JsonUtil.parseArray(jsonText);
            // 数组为空
            if (jsonArray.isEmpty()) {
                jsonErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.invalid.json"));
                return null;
            }

            // 判断：如果Array中除了对象类型还有其他的，那么提示错误
            if (jsonArray.stream().anyMatch(el -> !(el instanceof ObjectWrapper))) {
                jsonErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.invalid.json"));
                return null;
            }

            return (ObjectWrapper) jsonArray.get(0);
        } else if (JsonUtil.isJsonObject(jsonText)) {
            return JsonUtil.parseObject(jsonText);
        }


        // ----------------- Json5解析
        if (Json5Util.isJson5Array(jsonText)) {
            ArrayWrapper arrayWrapper = Json5Util.parseArray(jsonText);
            if (CollUtil.isEmpty(arrayWrapper)) {
                jsonErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.invalid.json"));
                return null;
            }

            // 判断：如果Array中除了对象类型还有其他的，那么提示错误
            if (arrayWrapper.stream().anyMatch(el -> !(el instanceof ObjectWrapper))) {
                jsonErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.invalid.json"));
                return null;
            }

            // 转为JsonObject
            return (ObjectWrapper) arrayWrapper.get(0);
        } else if (Json5Util.isJson5Object(jsonText)) {
            return Json5Util.parseObject(jsonText);
        }

        return null;
    }


    private String getFieldName(String originalKey) {
        String fieldName;
        // 包含下划线或空格，那就转为驼峰格式
        if (deserializerState.keepCamelCase && (originalKey.contains("_") || originalKey.contains(" "))) {
            return JsonAssistantUtil.toCamel(originalKey);
        }

        if (ReUtil.isMatch("^[A-Z]+$", originalKey)) {
            // 若为纯大写，则转为纯小写
            return originalKey.toLowerCase();
        }

        return StrUtil.lowerFirst(originalKey);
    }


    /**
     * 类名验证
     *
     * @author Memory
     * @since 2024/1/26
     */
    public static class ClassValidator implements InputValidatorEx {
        private final Project project;
        private final LanguageLevel level;

        public ClassValidator(Project project, PsiDirectory directory) {
            this.project = project;
            level = PsiUtil.getLanguageLevel(directory);
        }

        @Override
        public String getErrorText(String inputString) {
            if (!inputString.isEmpty() && !PsiNameHelper.getInstance(project).isQualifiedName(inputString)) {
                // return JavaErrorBundle.message("create.class.action.this.not.valid.java.qualified.name");
                return JsonAssistantBundle.messageOnSystem("error.illegal.class.name");
            }
            String shortName = StringUtil.getShortName(inputString);
            if (HighlightClassUtil.isRestrictedIdentifier(shortName, level)) {
                // return JavaErrorBundle.message("restricted.identifier", shortName);
                return JsonAssistantBundle.messageOnSystem("error.not.applicable.class.name", shortName);
            }
            return null;
        }

        @Override
        public boolean checkInput(String inputString) {
            return true;
        }

        @Override
        public boolean canClose(String inputString) {
            return !StringUtil.isEmptyOrSpaces(inputString) && getErrorText(inputString) == null;
        }
    }

    private class JsonValidatorDocumentListener implements DocumentListener {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            String json = jsonTextField.getText();
            getOKAction().setEnabled(JsonUtil.isJson(json) || Json5Util.isJson5(json));
        }
    }

}
