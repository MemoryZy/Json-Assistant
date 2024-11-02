package cn.memoryzy.json.model.formats;

import javax.swing.*;

/**
 * 操作（Action）相关信息
 *
 * @author Memory
 * @since 2024/11/2
 */
public class ActionInfo {

    /**
     * 当文本匹配成功，用于替换的操作名称
     */
    private String actionName;

    /**
     * 当文本匹配成功，用于替换的操作详情
     */
    private String actionDescription;

    /**
     * 当文本匹配成功，用于替换的操作图标
     */
    private Icon actionIcon;




    // ----------------------- GETTER/SETTER -----------------------

    public String getActionName() {
        return actionName;
    }

    public ActionInfo setActionName(String actionName) {
        this.actionName = actionName;
        return this;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public ActionInfo setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
        return this;
    }

    public Icon getActionIcon() {
        return actionIcon;
    }

    public ActionInfo setActionIcon(Icon actionIcon) {
        this.actionIcon = actionIcon;
        return this;
    }
}
