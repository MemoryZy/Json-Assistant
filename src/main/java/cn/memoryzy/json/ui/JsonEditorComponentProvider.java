package cn.memoryzy.json.ui;

import cn.memoryzy.json.util.UIManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/12/16
 */
public class JsonEditorComponentProvider {

    private final JsonStructureComponentProvider componentProvider;

    public JsonEditorComponentProvider(Project project) {
        this.componentProvider = new JsonStructureComponentProvider(null, UIManager.getWindowComponent(project), false);
    }

    public JComponent getComponent() {
        return componentProvider.getTreeComponent();
    }

    public JComponent getPreferredFocusedComponent() {
        return componentProvider.getTree();
    }

}
