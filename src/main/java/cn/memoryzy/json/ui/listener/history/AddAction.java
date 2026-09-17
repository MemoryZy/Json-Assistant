package cn.memoryzy.json.ui.listener.history;

import cn.memoryzy.json.bundle.JsonAssistantBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Memory
 * @since 2025/7/3
 */
public class AddAction extends AbstractAction {

    private final Runnable action;

    public AddAction(Runnable action) {
        super(JsonAssistantBundle.messageOnSystem("toolwindow.history.add.button"));
        this.action = action;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        action.run();
    }
}
