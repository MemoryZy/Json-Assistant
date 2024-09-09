package cn.memoryzy.json.ui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.HyperLinks;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.LombokAnnotationEnum;
import cn.memoryzy.json.ui.component.editor.CustomizedLanguageTextEditor;
import cn.memoryzy.json.ui.decorator.TextEditorErrorPopupDecorator;
import cn.memoryzy.json.util.JavaUtil;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.Notifications;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightClassUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.json.JsonLanguage;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.SwingHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private JBTextField classNameTextField;
    private EditorTextField jsonTextField;
    private TextEditorErrorPopupDecorator classNameErrorDecorator;
    private TextEditorErrorPopupDecorator jsonErrorDecorator;

    private final Project project;
    private final PsiDirectory directory;
    private final Module module;

    public JsonToJavaBeanDialog(@Nullable Project project, PsiDirectory directory, Module module) {
        super(project, true);
        this.project = project;
        this.directory = directory;
        this.module = module;

        setTitle(JsonAssistantBundle.message("json.to.javabean.title"));
        setOKButtonText(JsonAssistantBundle.messageOnSystem("json.to.javabean.ok.button.text"));
        setCancelButtonText(JsonAssistantBundle.messageOnSystem("json.to.javabean.cancel.button.text"));
        getOKAction().setEnabled(false);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        classNameTextField = new JBTextField();
        JBLabel label = new JBLabel(JsonAssistantBundle.messageOnSystem("json.window.label.text"));
        JPanel firstPanel = SwingHelper.newHorizontalPanel(Component.CENTER_ALIGNMENT, label, classNameTextField);
        firstPanel.setBorder(JBUI.Borders.emptyLeft(4));

        jsonTextField = new CustomizedLanguageTextEditor(JsonLanguage.INSTANCE, project, "", true);
        jsonTextField.setFont(JBUI.Fonts.create("Consolas", 15));
        jsonTextField.setPlaceholder(JsonAssistantBundle.messageOnSystem("json.window.placeholder.text") + PluginConstant.JSON_EXAMPLE);
        jsonTextField.setShowPlaceholderWhenFocused(true);
        jsonTextField.addDocumentListener(new JsonValidatorDocumentListener());

        classNameErrorDecorator = new TextEditorErrorPopupDecorator(getRootPane(), classNameTextField);
        jsonErrorDecorator = new TextEditorErrorPopupDecorator(getRootPane(), jsonTextField);

        JBSplitter splitter = new JBSplitter(true, 0.06f);
        splitter.setFirstComponent(firstPanel);
        splitter.setSecondComponent(jsonTextField);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(splitter, BorderLayout.CENTER);
        rootPanel.setPreferredSize(new Dimension(480, 450));
        return rootPanel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        List<Action> actions = new ArrayList<>();
        actions.add(getOKAction());
        actions.add(getCancelAction());
        actions.add(getHelpAction());
        return actions.toArray(new Action[0]);
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return classNameTextField;
    }

    @Override
    protected void doHelpAction() {
        BrowserUtil.browse(HyperLinks.JSON_TO_JAVA_BEAN_LINK);
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
            jsonErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("json.to.javabean.invalid.json.text"));
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
            classNameErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("json.to.javabean.already.exists.text", className));
            return false;
        }

        JSONObject jsonObject;

        // 解析Json
        if (!JsonUtil.isJsonStr(jsonText)) {
            jsonErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("json.to.javabean.invalid.json.text"));
            return false;
        }

        if (JsonUtil.isJsonArray(jsonText)) {
            JSONArray jsonArray = JSONUtil.parseArray(jsonText);
            // 数组为空
            if (jsonArray.isEmpty()) {
                jsonErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("json.to.javabean.invalid.json.text"));
                return false;
            }

            jsonObject = jsonArray.getJSONObject(0);
        } else if (JsonUtil.isJsonObject(jsonText)) {
            jsonObject = JSONUtil.parseObj(jsonText);
        } else {
            jsonObject = null;
        }

        // 属性为空
        if (Objects.isNull(jsonObject) || jsonObject.isEmpty()) {
            jsonErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("json.to.javabean.invalid.json.text"));
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
                // 判断是否存在lombok依赖
                if (JavaUtil.hasLibrary(module, PluginConstant.LOMBOK_LIB)) {
                    // 添加lombok注解，递归给内部类也加上
                    importClass(project, newClass, factory);
                }

                // 导入
                JavaUtil.importClassesInClass(project, newClass, needImportList.toArray(new String[0]));

                // 刷新文件系统
                PlatformUtil.refreshFileSystem();
                // 编辑器定位到新建类
                newClass.navigate(true);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });

        return true;
    }


    private void recursionAddProperty(JSONObject jsonObject, PsiClass psiClass, PsiElementFactory factory, Set<String> needImportList) {
        // 循环所有Json字段
        for (Map.Entry<String, Object> entry : jsonObject) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // ------------- 如果value是JsonObject，表示是对象
            if (value instanceof JSONObject) {
                JSONObject childJsonObject = (JSONObject) value;
                // 如果是对象，则还需要创建内部类
                PsiClass innerClass = factory.createClass(StrUtil.upperFirst(key));

                // 添加 static 关键字
                PsiKeyword keyword = factory.createKeyword(PsiModifier.STATIC);
                PsiModifierList modifierList = innerClass.getModifierList();
                if (Objects.nonNull(modifierList)) {
                    modifierList.add(keyword);
                }

                // 则递归添加
                recursionAddProperty(childJsonObject, innerClass, factory, needImportList);
                // 添加内部类至主类
                psiClass.add(innerClass);
                // 添加当前内部类类型的字段
                String fieldText = StrUtil.format("{} {} {};", PsiModifier.PRIVATE, innerClass.getName(), StrUtil.lowerFirst(key));
                // 构建字段对象
                PsiField psiField = factory.createFieldFromText(fieldText, psiClass);
                // 添加到Class
                psiClass.add(psiField);
            } else if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                if (CollUtil.isNotEmpty(jsonArray)) {
                    String innerClassName;
                    Object element = jsonArray.get(0);
                    if (element instanceof JSONObject) {
                        JSONObject jsonObj = (JSONObject) element;
                        // 对象值
                        innerClassName = StrUtil.upperFirst(key + "Bean");
                        // 如果是对象，则还需要创建内部类
                        PsiClass innerClass = factory.createClass(innerClassName);
                        // 添加 static 关键字
                        PsiKeyword keyword = factory.createKeyword(PsiModifier.STATIC);
                        PsiModifierList modifierList = innerClass.getModifierList();
                        if (Objects.nonNull(modifierList)) {
                            modifierList.add(keyword);
                        }
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
                    String fieldText = StrUtil.format("{} List<{}> {};", PsiModifier.PRIVATE, innerClassName, StrUtil.lowerFirst(key));
                    // 构建字段对象
                    PsiField psiField = factory.createFieldFromText(fieldText, psiClass);
                    // 添加到Class
                    psiClass.add(psiField);
                }
            } else {
                // ------------- 非对象，则直接添加字段
                // 获取字段类型
                String propertyType = JavaUtil.getStrType(value);
                // 定义字段文本
                String fieldText = StrUtil.format("{} {} {};", PsiModifier.PRIVATE, propertyType, StrUtil.lowerFirst(key));

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
                    Notifications.showLogNotification(JsonAssistantBundle.messageOnSystem("notify.json.to.javabean.incorrect.field.text", key), NotificationType.ERROR, project);
                    throw e;
                }

                // 添加到Class
                psiClass.add(psiField);
            }
        }
    }


    /**
     * 导入类并添加注解
     *
     * @param project  当前项目
     * @param newClass 待导入的类
     * @param factory  用于创建新的psi元素的工厂
     */
    private void importClass(Project project, PsiClass newClass, PsiElementFactory factory) {
        // 增加注解
        PsiAnnotation dataAnnotation = factory.createAnnotationFromText(
                "@" + StringUtil.getShortName(LombokAnnotationEnum.DATA.getValue()), null);
        PsiAnnotation accessorsAnnotation = factory.createAnnotationFromText(
                "@" + StringUtil.getShortName(LombokAnnotationEnum.ACCESSORS.getValue()) + "(chain = true)", null);

        // 导入类
        JavaUtil.importClassesInClass(project, newClass, LombokAnnotationEnum.DATA.getValue(), LombokAnnotationEnum.ACCESSORS.getValue());

        PsiElement firstChild = newClass.getFirstChild();
        if (firstChild instanceof PsiDocComment) {
            newClass.addAfter(accessorsAnnotation, firstChild);
            newClass.addAfter(dataAnnotation, firstChild);
        } else {
            newClass.addBefore(dataAnnotation, firstChild);
            newClass.addBefore(accessorsAnnotation, firstChild);
        }

        // 内部类也加上（要递归）
        this.addImportAnnotationOnInnerClass(newClass, dataAnnotation, accessorsAnnotation);
    }

    private void addImportAnnotationOnInnerClass(PsiClass newClass, PsiAnnotation dataAnnotation, PsiAnnotation accessorsAnnotation) {
        PsiClass[] innerClasses = newClass.getInnerClasses();
        if (ArrayUtil.isNotEmpty(innerClasses)) {
            for (PsiClass innerClass : innerClasses) {
                PsiElement innerFirstChild = innerClass.getFirstChild();
                innerClass.addBefore(dataAnnotation, innerFirstChild);
                innerClass.addBefore(accessorsAnnotation, innerFirstChild);
                // 找内部类中是否还有内部类，并添加上注解
                addImportAnnotationOnInnerClass(innerClass, dataAnnotation, accessorsAnnotation);
            }
        }
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
                return JsonAssistantBundle.messageOnSystem("json.to.javabean.illegal.class.name.text");
            }
            String shortName = StringUtil.getShortName(inputString);
            if (HighlightClassUtil.isRestrictedIdentifier(shortName, level)) {
                // return JavaErrorBundle.message("restricted.identifier", shortName);
                return JsonAssistantBundle.messageOnSystem("json.to.javabean.not.applicable.class.name.text", shortName);
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
            getOKAction().setEnabled(JsonUtil.isJsonStr(json));
        }
    }

}
