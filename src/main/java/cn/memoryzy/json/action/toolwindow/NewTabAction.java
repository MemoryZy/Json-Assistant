package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/20
 */
public class NewTabAction extends DumbAwareAction {

    private final ContentFactory contentFactory;
    private final ToolWindowEx toolWindow;

    public NewTabAction(ContentFactory contentFactory, ToolWindowEx toolWindow) {
        super();
        this.contentFactory = contentFactory;
        this.toolWindow = toolWindow;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.new.tab.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.new.tab.description"));
        presentation.setIcon(AllIcons.General.Add);

        registerCustomShortcutSet(CustomShortcutSet.fromString("ctrl shift T"), toolWindow.getComponent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        JsonAssistantUtil.addNewContent(event.getProject(), toolWindow, contentFactory, FileTypeHolder.JSON);
    }
}
