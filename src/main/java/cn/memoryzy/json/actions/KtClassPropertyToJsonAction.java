package cn.memoryzy.json.actions;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.JavaUtil;
import cn.memoryzy.json.utils.KtUtil;
import cn.memoryzy.json.utils.Notification;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author Memory
 * @since 2024/7/5
 */
public class KtClassPropertyToJsonAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(KtClassPropertyToJsonAction.class);

    public KtClassPropertyToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.kt.class.property.to.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.kt.class.property.to.json.description"));
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        PsiClass currentPsiClass = KtUtil.getPsiClass(event);
        Map<String, Object> jsonMap = new TreeMap<>();

        try {
            // 递归添加所有属性，包括嵌套属性
            JavaUtil.recursionAddProperty(currentPsiClass, jsonMap);
        } catch (Error e) {
            LOG.error(e);
            // 给通知
            Notification.notify(JsonAssistantBundle.messageOnSystem("notify.javabean.to.json.tip.recursion"), NotificationType.ERROR, project);
            return;
        }

        // 将Map转换为Json
        String jsonStr = JSONUtil.toJsonStr(jsonMap, JSONConfig.create().setStripTrailingZeros(false));
        // 添加至剪贴板
        PlatformUtil.setClipboard(jsonStr);
        // 给通知
        Notification.notify(JsonAssistantBundle.messageOnSystem("notify.javabean.to.json.tip.copy"), NotificationType.INFORMATION, project);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 设置可见性
        e.getPresentation().setEnabledAndVisible(Objects.nonNull(e.getProject()) && KtUtil.isKtFile(e) && KtUtil.hasKtProperty(e));
    }
}