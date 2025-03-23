package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonValueHandleType;
import cn.memoryzy.json.util.JsonValueHandler;
import com.intellij.json.psi.JsonFile;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/2/25
 */
public class ExpandAllNestedJsonAction extends DumbAwareAction implements UpdateInBackground {

    public ExpandAllNestedJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.expand.nested.Json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.expand.nested.Json.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        DataContext dataContext = e.getDataContext();
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        if (psiFile instanceof JsonFile) {
            JsonValueHandler.handleAllElement(project, psiFile, JsonValueHandleType.NESTED_JSON);
        } else {
            JsonValueHandler.handleAllWrapper(project, dataContext, JsonValueHandleType.NESTED_JSON);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(JsonValueHandler.containsSpecialType(e.getDataContext(), JsonValueHandleType.NESTED_JSON));
    }
}
