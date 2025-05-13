package cn.memoryzy.json.ui.component;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionButtonLook;
import com.intellij.openapi.actionSystem.impl.ActionButton;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/12/29
 */
public class SearchHistoryButton extends ActionButton {

    public SearchHistoryButton(AnAction action, boolean focusable) {
        super(action, action.getTemplatePresentation().clone(), ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);
        setLook(ActionButtonLook.INPLACE_LOOK);
        setFocusable(focusable);
        updateIcon();
    }

    @Override
    protected DataContext getDataContext() {
        return DataManager.getInstance().getDataContext(this);
    }

    @Override
    public int getPopState() {
        return isSelected() ? ActionButtonComponent.SELECTED : super.getPopState();
    }

    @Override
    public Icon getIcon() {
        if (isEnabled() && isSelected()) {
            Icon selectedIcon = myPresentation.getSelectedIcon();
            if (selectedIcon != null) return selectedIcon;
        }
        return super.getIcon();
    }

}
