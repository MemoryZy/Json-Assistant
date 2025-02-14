package cn.memoryzy.json.action.debug;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree;
import icons.JsonAssistantIcons;
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
        presentation.setText(JsonAssistantBundle.message("action.serialize.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.serialize.description"));
        presentation.setIcon(JsonAssistantIcons.JSON);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        XDebuggerTree tree = XDebuggerTree.getTree(e.getDataContext());

        System.out.println();
    }
}
