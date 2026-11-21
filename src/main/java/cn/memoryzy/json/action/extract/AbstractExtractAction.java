package cn.memoryzy.json.action.extract;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonExtractUtil;
import cn.memoryzy.json.util.Notifications;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 「提取」分组下动作的公共父类
 * <p>
 * 统一处理三件事：
 * 1. 只有在「以 JSON / JSON5 格式打开的编辑器」中才展示，普通文本编辑器里不出现；
 * 2. 光标处没有可提取的内容（例如光标落在空白处、键没有值）时隐藏；
 * 3. 取到文本后复制到剪贴板并给出通知。
 * <p>
 * 子类只需要实现 {@link #computeText(PsiElement)}，返回 null 表示当前光标不可提取。
 *
 * @author Memory
 * @since 2025/11/21
 */
public abstract class AbstractExtractAction extends DumbAwareAction implements UpdateInBackground {

    protected AbstractExtractAction(String textKey, String descriptionKey, @Nullable Icon icon) {
        super(JsonAssistantBundle.message(textKey), JsonAssistantBundle.messageOnSystem(descriptionKey), icon);
    }

    /**
     * 计算需要复制到剪贴板的文本
     *
     * @param element 光标处的 Psi 元素
     * @return 待复制的文本；返回 null 表示当前光标处不可提取（动作将被隐藏）
     */
    @Nullable
    protected abstract String computeText(@Nullable PsiElement element);

    @Override
    public void update(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        // PSI 的访问需要读锁（update 可能在后台线程被调用）
        // [0] 是否展示：[1] 是否可执行
        boolean[] state = ReadAction.compute(() -> {
            if (!JsonExtractUtil.isJsonEditor(dataContext)) {
                return new boolean[]{false, false};
            }

            PsiElement element = JsonExtractUtil.getCaretElement(project, dataContext);
            // 展示以「光标处是否存在 Json 节点」为准，四个动作同进同退
            boolean visible = JsonExtractUtil.resolveCaretValue(element) != null;
            return new boolean[]{visible, visible && computeText(element) != null};
        });

        event.getPresentation().setVisible(state[0]);
        event.getPresentation().setEnabled(state[1]);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        // 同样需要在读锁内读取 PSI，避免在非读上下文访问语法树
        String text = ReadAction.compute(() -> computeText(JsonExtractUtil.getCaretElement(project, dataContext)));
        if (text == null) {
            return;
        }

        PlatformUtil.setClipboard(text);
        Notifications.showNotification(JsonAssistantBundle.messageOnSystem("notification.extract.copy"), NotificationType.INFORMATION, event.getProject());
    }

}