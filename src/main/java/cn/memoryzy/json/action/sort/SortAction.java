package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.model.sort.SortStrategy;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2025/9/2
 */
public abstract class SortAction extends DumbAwareAction implements UpdateInBackground {

    public SortAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }

    protected abstract SortStrategy getStrategy();

}
