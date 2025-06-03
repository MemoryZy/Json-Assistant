package cn.memoryzy.json.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2025/6/3
 */
public class ModifyValueDialog extends DialogWrapper {
    public ModifyValueDialog(@Nullable Project project) {
        super(project, true);

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JBLabel label = new JBLabel("Address");
        label.setBorder(JBUI.Borders.emptyLeft(5));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 17));
        TitledSeparator titledSeparator = new TitledSeparator();

        JPanel panel = SwingHelper.newLeftAlignedVerticalPanel(label, titledSeparator);


        return new BorderLayoutPanel().addToTop(panel);
    }
}
