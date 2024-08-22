package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/8/23
 */
public class SupportDialog2 extends DialogWrapper {

    public SupportDialog2() {
        super((Project) null, true);

        setModal(false);
        setTitle(JsonAssistantBundle.messageOnSystem("dialog.support.support.header"));
        setOKButtonText(JsonAssistantBundle.messageOnSystem("dialog.support.ok.text"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return null;
    }
}
