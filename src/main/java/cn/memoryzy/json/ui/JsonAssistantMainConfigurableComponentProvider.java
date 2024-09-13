package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginConstant;
import com.intellij.ide.DataManager;
import com.intellij.openapi.options.ex.Settings;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBLabel;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Memory
 * @since 2024/9/13
 */
public class JsonAssistantMainConfigurableComponentProvider {

    private JPanel rootPanel;
    private JBLabel mainConfigurableDescription;
    private ActionLink JsonViewConfigurableLink;

    public JsonAssistantMainConfigurableComponentProvider() {
        mainConfigurableDescription.setText(JsonAssistantBundle.messageOnSystem("plugin.main.configurable.description"));
        JsonViewConfigurableLink.setText(JsonAssistantBundle.message("plugin.editor.options.configurable.displayName"));

        JsonViewConfigurableLink.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Settings settings = Settings.KEY.getData(DataManager.getInstance().getDataContext((ActionLink) e.getSource()));
                if (settings != null) {
                    settings.select(settings.find(PluginConstant.JSON_VIEWER_CONFIGURABLE_ID));
                }
            }
        });
    }

    public JComponent createRootPanel() {
        return rootPanel;
    }

}
