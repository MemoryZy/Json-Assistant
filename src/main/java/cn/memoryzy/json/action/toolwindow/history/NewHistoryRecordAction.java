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
 * 目前来看，可能没有主动添加历史记录的必要
 *
 * @author Memory
 * @since 2025/11/24
 */
public class NewHistoryRecordAction extends DumbAwareBaseAction {

    private final HistoryToolWindowComponentProvider provider;

    public NewHistoryRecordAction(HistoryToolWindowComponentProvider provider) {
        super(JsonAssistantBundle.messageOnSystem("action.new.historyRecord.text"),null, AllIcons.FileTypes.Json);
        setEnabledInModalContext(true);
        this.provider = provider;
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        newRecord(project, provider);
    }

    public static void newRecord(Project project, HistoryToolWindowComponentProvider provider) {
        if (project == null) return;

        // 要考虑根节点的情况
        List<HistoryNode> selectedNodes = provider.getSelectedRecordNodes();
        // 第一次新增时的情况
        if (selectedNodes.isEmpty()) {
            provider.showNewRecordPopup(project, provider.getRootHistoryNode());
            return;
        }

        // 检查选中节点的类型和数量
        List<HistoryNode> historyNodes = selectedNodes.stream().filter(HistoryNode::isRecord).collect(Collectors.toList());
        List<HistoryNode> groupNodes = selectedNodes.stream().filter(node -> node.isGroup() || node.isRoot()).collect(Collectors.toList());

        // 2. 选中了多个分组节点 - 弹出选择窗口
        if (groupNodes.size() > 1) {
            provider.showTargetGroupSelectionDialog(project, groupNodes, false);
            return;
        }

        // 3. 选中了一个记录节点和一个分组节点 -> 直接在分组下新增
        // 4. 选中了多个记录和多个分组 -> 弹出选择窗口 (逻辑同2)
        if (CollUtil.isNotEmpty(groupNodes)) {
            // 选中了一个分组，不管选中了几个记录
            provider.showNewRecordPopup(project, groupNodes.get(0));

        } else if (historyNodes.size() == 1) {
            // 只选中了一个记录节点，在其所在分组下创建
            provider.showNewRecordPopup(project, historyNodes.get(0).getParentNode());
        }
    }

}
