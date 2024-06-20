package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class JsonStructureAnywhereAction extends AnAction {

    public JsonStructureAnywhereAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.structure.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

    }
}
