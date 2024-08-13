package cn.memoryzy.json.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/8/14
 */
public class Test extends DialogWrapper {
    public Test(@Nullable Project project) {
        super(project, false);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        Splitter splitter = new Splitter(true, 0.3f);
        splitter.setFirstComponent(new JLabel("aaaaaaaaaaaa"));
        splitter.setSecondComponent(new JBTextField("xxxxxxxxxxx"));

        return splitter;
    }
}
