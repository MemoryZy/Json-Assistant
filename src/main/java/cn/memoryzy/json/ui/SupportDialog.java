package cn.memoryzy.json.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/7/29
 */
public class SupportDialog extends DialogWrapper {

    private JPanel rootPanel;

    public SupportDialog(Project project) {
        super(project, true);
        setModal(false);


    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return null;
    }
}
