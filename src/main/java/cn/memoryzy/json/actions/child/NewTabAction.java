package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonViewerWindow;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/20
 */
public class NewTabAction extends DumbAwareAction {

    private final ContentFactory contentFactory;
    private final ToolWindowEx toolWindowEx;

    public NewTabAction(ContentFactory contentFactory, ToolWindowEx toolWindowEx) {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.new.tab.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.new.tab.description"));
        presentation.setIcon(AllIcons.General.Add);

        this.contentFactory = contentFactory;
        this.toolWindowEx = toolWindowEx;

        registerCustomShortcutSet(CustomShortcutSet.fromString("ctrl shift T"), toolWindowEx.getComponent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        JsonViewerWindow window = new JsonViewerWindow(e.getProject());
        Content content = contentFactory.createContent(window.getRootPanel(), "Json2", false);
        toolWindowEx.getContentManager().addContent(content);
    }
}
