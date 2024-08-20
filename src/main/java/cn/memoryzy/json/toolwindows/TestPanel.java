package cn.memoryzy.json.toolwindows;

import cn.memoryzy.json.actions.child.NewTabAction;
import cn.memoryzy.json.ui.basic.CustomizedLanguageTextEditor;
import com.intellij.execution.impl.ConsoleViewUtil;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.tools.SimpleActionGroup;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/8/20
 */
public class TestPanel {

    private final Project project;

    public TestPanel(Project project) {
        this.project = project;
    }


    public JComponent getComponent() {
        SimpleToolWindowPanel simpleToolWindowPanel = new SimpleToolWindowPanel(false, true);

        SimpleActionGroup simpleActionGroup = new SimpleActionGroup();
        simpleActionGroup.add(new AnAction("xxxxxxx") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }
        });

        // ActionGroup toolbarGroup = (ActionGroup) ActionManager.getInstance().getAction(TOOLBAR_GROUP);
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, simpleActionGroup, false);

        CustomizedLanguageTextEditor languageTextEditor = new CustomizedLanguageTextEditor(JsonLanguage.INSTANCE, project, "", false);

        simpleToolWindowPanel.setToolbar(toolbar.getComponent());
        simpleToolWindowPanel.setContent(languageTextEditor);

        return simpleToolWindowPanel;
    }
}
