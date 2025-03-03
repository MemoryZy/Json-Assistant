package cn.memoryzy.json.ui.dialog;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.ui.decorator.TextEditorErrorPopupDecorator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2025/2/27
 */
public class DiffNameDialog extends DialogWrapper {
    private JPanel rootPanel;
    private JBLabel leftSideLabel;
    private JBLabel rightSideLabel;
    private JBTextField leftSideTextField;
    private JBTextField rightSideTextField;

    private final JBLabel leftSourceLabel;
    private final JBLabel rightSourceLabel;

    private final TextEditorErrorPopupDecorator leftErrorDecorator;
    private final TextEditorErrorPopupDecorator rightErrorDecorator;

    public DiffNameDialog(JBLabel leftSourceLabel, JBLabel rightSourceLabel) {
        super((Project) null);
        this.leftSourceLabel = leftSourceLabel;
        this.rightSourceLabel = rightSourceLabel;

        JRootPane rootPane = getRootPane();
        this.leftErrorDecorator = new TextEditorErrorPopupDecorator(rootPane, leftSideTextField);
        this.rightErrorDecorator = new TextEditorErrorPopupDecorator(rootPane, rightSideTextField);

        setTitle(JsonAssistantBundle.messageOnSystem("dialog.diffName.title"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        leftSideLabel.setText(JsonAssistantBundle.messageOnSystem("dialog.diffName.left.label"));
        rightSideLabel.setText(JsonAssistantBundle.messageOnSystem("dialog.diffName.right.label"));
        leftSideTextField.setText(leftSourceLabel.getText());
        rightSideTextField.setText(rightSourceLabel.getText());
        return rootPanel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return leftSideTextField;
    }

    @Override
    public void show() {
        ApplicationManager.getApplication().invokeLater(super::show);
    }

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return UrlType.DEFAULT.getId();
    }


    @Override
    protected void doOKAction() {
        if (getOKAction().isEnabled()) {
            // 执行逻辑
            if (executeOkAction()) {
                close(OK_EXIT_CODE);
            }
        }
    }

    private boolean executeOkAction() {
        String leftSideName = leftSideTextField.getText();
        String rightSideName = rightSideTextField.getText();

        if (StrUtil.isBlank(leftSideName)) {
            leftErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.diffName.blank"));
            return false;
        }

        if (StrUtil.isBlank(rightSideName)) {
            rightErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.diffName.blank"));
            return false;
        }

        if (leftSideName.length() > 60) {
            leftErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.diffName.tooLong"));
            return false;
        }

        if (rightSideName.length() > 60) {
            rightErrorDecorator.setError(JsonAssistantBundle.messageOnSystem("error.diffName.tooLong"));
            return false;
        }

        // 修改标题
        leftSourceLabel.setText(leftSideName);
        rightSourceLabel.setText(rightSideName);

        return true;
    }

}
