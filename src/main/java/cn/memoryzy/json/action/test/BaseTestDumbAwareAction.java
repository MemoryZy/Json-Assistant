package cn.memoryzy.json.action.test;

import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;

/**
 * @author Memory
 * @since 2025/12/9
 */
public abstract class BaseTestDumbAwareAction extends DumbAwareAction implements UpdateInBackground {

    public BaseTestDumbAwareAction() {
        getTemplatePresentation().setText(getActionText());
    }

    public abstract String getActionId();

    public abstract String getActionText();

    public abstract String getParentGroupId();

    public String getIconPath() {
        return null;
    }

    public Constraints getConstraints() {
        return Constraints.LAST;
    }
    

}