package cn.memoryzy.json.action.debug;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.enums.FileTypes;
import cn.memoryzy.json.model.RecursionContext;
import cn.memoryzy.json.model.RecursiveResult;
import cn.memoryzy.json.util.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.sun.jdi.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author Memory
 * @since 2025/2/14
 */
public class RuntimeObjectToJsonAction extends AnAction implements UpdateInBackground {

    private static final int MAX_DEPTH = 8;
    private static final Logger LOG = Logger.getInstance(RuntimeObjectToJsonAction.class);
    public static final Key<Boolean> RESOLVE_COMMENT_KEY = Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".RESOLVE_COMMENT");

    public RuntimeObjectToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.serialize.runtime.object.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.serialize.runtime.object.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        handleObjectReferenceResolution(e.getProject(), dataContext, JsonUtil::toJsonStr, false);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(isEnabled(e.getProject(), e.getDataContext()));
    }

    private static boolean isEnabled(@Nullable Project project, DataContext dataContext) {
        Class<?> languageClz = JsonAssistantUtil.getClassByName(FileTypes.JAVA.getLanguageQualifiedName());
        Class<?> classClz = JsonAssistantUtil.getClassByName("com.intellij.psi.PsiClass");
        if (project != null && languageClz != null && classClz != null) {
            return JavaDebugUtil.isObjectOrListWithChildren(project, dataContext);
        }

        return false;
    }

    public static void handleObjectReferenceResolution(Project project, @NotNull DataContext dataContext, Function<Object, String> jsonConverter, boolean resolveComment) {
        Application application = ApplicationManager.getApplication();
        new Task.Backgroundable(project, JsonAssistantBundle.messageOnSystem("progress.convert.to.json.title"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                indicator.setText(JsonAssistantBundle.messageOnSystem("progress.convert.to.json.text"));
                indicator.setFraction(0);

                RecursiveResult result;
                try {
                    // 保存【是否解析注释】
                    if (resolveComment) project.putUserData(RESOLVE_COMMENT_KEY, true);
                    // 调整进度
                    indicator.setFraction(0.1);
                    // 解析树节点值
                    Value value = JavaDebugUtil.parseTreeNode(dataContext);
                    if (null == value) return;
                    indicator.setFraction(0.3);

                    RecursionContext context = new RecursionContext(MAX_DEPTH);

                    // 调用READ线程执行
                    Object jsonValue = application.runReadAction(
                            (Computable<Object>) () -> JavaDebugUtil.getValue(project, value, context));

                    // 包装结果
                    result = new RecursiveResult(jsonValue, context);

                    // 调整进度
                    indicator.setFraction(0.9);
                    Object resultValue = result.getValue();

                    // 写入窗口
                    if (resultValue != null) {
                        // 处理递归警告
                        if (result.isRecursionLimitReached()) {
                            handleRecursionWarning(project, result);
                        }

                        application.invokeLater(() -> ToolWindowUtil.addNewContentWithEditorContentIfNeeded(project, jsonConverter.apply(resultValue), FileTypeHolder.JSON5));
                    }
                } catch (StackOverflowError ex) {
                    LOG.error(ex);
                    Notifications.showNotification(JsonAssistantBundle.messageOnSystem("error.runtime.serialize.recursion"), NotificationType.ERROR, project);
                } finally {
                    // 置空
                    project.putUserData(RESOLVE_COMMENT_KEY, null);
                }

                indicator.setFraction(1.0);
                indicator.setText(JsonAssistantBundle.messageOnSystem("progress.convert.to.json.finished.text"));
            }
        }.queue();
    }

    private static void handleRecursionWarning(Project project, RecursiveResult result) {
        // String message = String.format(
        //         JsonAssistantBundle.messageOnSystem("warning.runtime.serialize.max_depth"),
        //         result.getCurrentDepth(),
        //         result.getMaxDepth()
        // );
        //
        // Notifications.showNotification(message, NotificationType.WARNING, project);
    }

}
