package cn.memoryzy.json.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2025/11/24
 */
public abstract class BaseAction extends AnAction implements UpdateInBackground {

    public BaseAction() {
    }

    public BaseAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(isActionEnabled(e));
    }

    /**
     * 判断操作是否启用
     */
    protected boolean isActionEnabled(@NotNull AnActionEvent e) {
        // 默认实现：只有在项目中且有一定条件时才启用
        return e.getProject() != null;
    }
}
