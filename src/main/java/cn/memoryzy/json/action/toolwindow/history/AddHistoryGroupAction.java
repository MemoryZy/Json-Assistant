package cn.memoryzy.json.action.toolwindow.history;

import cn.hutool.core.collection.CollUtil;
import cn.memoryzy.json.action.DumbAwareBaseAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import cn.memoryzy.json.ui.tree.HistoryNode;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2025/9/10
 */
public class AddHistoryGroupAction extends DumbAwareBaseAction {

    private final HistoryToolWindowComponentProvider provider;

    public AddHistoryGroupAction(HistoryToolWindowComponentProvider provider) {
        super(JsonAssistantBundle.messageOnSystem("action.new.historyGroup.text"), null, AllIcons.Nodes.ModuleGroup);
        this.provider = provider;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (null == project) return;

        // 要考虑根节点的情况
        List<HistoryNode> selectedNodes = provider.getSelectedRecordNodes();
        // 检查选中节点的类型和数量
        List<HistoryNode> historyNodes = selectedNodes.stream().filter(HistoryNode::isRecord).collect(Collectors.toList());
        List<HistoryNode> groupNodes = selectedNodes.stream().filter(node -> node.isGroup() || node.isRoot()).collect(Collectors.toList());

        // 2. 选中了多个分组节点 - 弹出选择窗口
        if (groupNodes.size() > 1) {
            provider.showTargetGroupSelectionDialog(project, groupNodes, true);
            return;
        }

        // 3. 选中了一个记录节点和一个分组节点 -> 直接在分组下新增
        // 4. 选中了多个记录和多个分组 -> 弹出选择窗口 (逻辑同2)
        if (CollUtil.isNotEmpty(groupNodes)) {
            // 选中了一个分组，不管选中了几个记录
            provider.showNewGroupPopup(project, groupNodes.get(0));

        } else if (historyNodes.size() == 1) {
            // 只选中了一个记录节点，在其所在分组下创建
            provider.showNewGroupPopup(project, historyNodes.get(0).getParentNode());
        }
    }

}
