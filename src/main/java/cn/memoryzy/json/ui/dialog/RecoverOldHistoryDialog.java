package cn.memoryzy.json.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/11/26
 */
public class RecoverOldHistoryDialog extends DialogWrapper {

    private final Project project;

    public RecoverOldHistoryDialog(@Nullable Project project) {
        super(project, true);
        this.project = project;

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JBLabel label = new JBLabel("检测到旧版本存在20条历史记录，是否导入到新版本中？");

        BorderLayoutPanel borderLayoutPanel = new BorderLayoutPanel();
        borderLayoutPanel.addToCenter(label);
        return borderLayoutPanel;
    }
}
