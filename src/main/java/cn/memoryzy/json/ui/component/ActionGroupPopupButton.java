package cn.memoryzy.json.ui.component;

import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @author Memory
 * @since 2025/4/4
 */
public class ActionGroupPopupButton extends ActionButton {

    private static final Logger LOG = Logger.getInstance(ActionGroupPopupButton.class);

    public ActionGroupPopupButton(@NotNull AnAction action, Presentation presentation, String place, @NotNull Dimension minimumSize) {
        super(action, presentation, place, minimumSize);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void showActionGroupPopup(@NotNull ActionGroup actionGroup, @NotNull AnActionEvent event) {
        HelpTooltip.hide(this);
        ActionUtil.performActionDumbAwareWithCallbacks(myAction, event, event.getDataContext());
    }

    @Override
    protected boolean shallPaintDownArrow() {
        return true;
    }
}
