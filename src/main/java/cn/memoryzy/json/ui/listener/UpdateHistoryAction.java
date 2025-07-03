package cn.memoryzy.json.ui.listener;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.state.v2.JsonRecord;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Memory
 * @since 2025/7/3
 */
public class UpdateHistoryAction extends AbstractAction {

    private JsonRecord record;

    public UpdateHistoryAction() {
        super(JsonAssistantBundle.messageOnSystem("toolwindow.history.update.button"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (null == record) return;



        // 1.判断Json编辑器内是否是正确文本

        // 2.名称有没有超过限制

        // 3.保存
        // historyManager.

        // record.setName(nameEditorWrapper.getText())
        //         .setRawText(recordEditor.getDocument().getText())
        //         .setUpdateTime(System.currentTimeMillis())
        // .setWrapper()
        // .setDisplayText()

        // JComponent component = recordEditor.getComponent();
        // JComponent contentComponent = recordEditor.getContentComponent();

        System.out.println();
    }

    public UpdateHistoryAction setRecord(JsonRecord record) {
        this.record = record;
        return this;
    }
}
