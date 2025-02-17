package cn.memoryzy.json.action.debug;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.util.JavaDebugUtil;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.ToolWindowUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/2/14
 */
public class RuntimeObjectToJsonAction extends AnAction {

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
        Object result = JavaDebugUtil.resolveObjectReference(dataContext);
        if (result != null) {
            ToolWindowUtil.addNewContentWithEditorContentIfNeeded(e.getProject(), JsonUtil.toJsonStr(result), FileTypeHolder.JSON5);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(JavaDebugUtil.isObjectOrListWithChildren(e.getDataContext()));
    }
}
