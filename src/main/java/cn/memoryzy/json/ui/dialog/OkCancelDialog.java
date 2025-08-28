package cn.memoryzy.json.ui.dialog;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2025/8/1
 */
public class OkCancelDialog extends DialogWrapper {

    private final Icon icon;
    private final String content;
    private final JBCheckBox doNotAsk;
    private DoNotAskOption doNotAskOption;

    public OkCancelDialog(String title, String content, Icon icon) {
        super((Project) null, true);
        setTitle(title);
        this.icon = icon;
        this.content = content;
        this.doNotAsk = new JBCheckBox(JsonAssistantBundle.messageOnSystem("dialog.options.do.not.ask"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JBLabel iconLabel = new JBLabel(icon);
        JBLabel contentLabel = new JBLabel(JsonAssistantUtil.wrapHtml(content));
        contentLabel.setBorder(JBUI.Borders.emptyLeft(20));
        JPanel panel = SwingHelper.newHorizontalPanel(Component.CENTER_ALIGNMENT, iconLabel, contentLabel);
        panel.setPreferredSize(new Dimension(JBUIScale.scale(350), JBUIScale.scale(50)));
        return panel;
    }

    @Override
    protected JComponent createSouthPanel() {
        JComponent southPanel = super.createSouthPanel();
        return new BorderLayoutPanel().addToLeft(doNotAsk).addToRight(southPanel);
    }

    public OkCancelDialog doNotAsk(DoNotAskOption doNotAskOption) {
        this.doNotAskOption = doNotAskOption;
        return this;
    }

    public boolean ask() {
        boolean isOk = showAndGet();
        if (null != doNotAskOption) {
            doNotAskOption.rememberChoice(doNotAsk.isSelected(), getExitCode());
        }
        return isOk;
    }

    public interface DoNotAskOption {
        void rememberChoice(boolean isSelected, int exitCode);
    }

}
