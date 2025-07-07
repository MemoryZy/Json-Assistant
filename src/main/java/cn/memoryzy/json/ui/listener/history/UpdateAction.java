package cn.memoryzy.json.ui.listener.history;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.state.JsonRecord;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * @author Memory
 * @since 2025/7/3
 */
public class UpdateAction extends AbstractAction {

    private JsonRecord record;
    private final Consumer<JsonRecord> action;

    public UpdateAction(Consumer<JsonRecord> action) {
        super(JsonAssistantBundle.messageOnSystem("toolwindow.history.update.button"));
        this.action = action;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        action.accept(record);
    }

    public UpdateAction setRecord(JsonRecord record) {
        this.record = record;
        return this;
    }
}
