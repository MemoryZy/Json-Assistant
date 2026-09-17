package cn.memoryzy.json.action.toolwindow.history;

import cn.hutool.core.collection.CollUtil;
import cn.memoryzy.json.action.DumbAwareBaseActionGroup;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import cn.memoryzy.json.ui.tree.HistoryNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Memory
 * @since 2025/9/12
 */
public class NewHistoryGroup extends DumbAwareBaseActionGroup {

    private final HistoryToolWindowComponentProvider provider;

    public NewHistoryGroup(HistoryToolWindowComponentProvider provider, SimpleToolWindowPanel windowPanel) {
        super(JsonAssistantBundle.messageOnSystem("action.new.history.text"), null, IconUtil.getAddIcon());
        setPopup(true);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt INSERT"), windowPanel);
        this.provider = provider;
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        return new AnAction[]{
                new AddHistoryGroupAction(provider),
                new Separator(),
                new NewHistoryRecordAction(provider)
        };
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(isNewOperationVisible(e));
    }

    /**
     * 检查是否允许显示新增操作
     *
     * <p>该方法用于判断在树形结构中选择节点时，是否满足显示“新增”操作的条件。
     * 当未选中任何节点、选中了多个记录节点时，不显示新增操作。
     * 仅当选中一个且仅一个记录节点，或仅选中分组节点时，才显示新增操作。</p>
     *
     * <p>1.当选中了多个记录节点的话，不显示新增操作；
     * 2.当选中了多个分组节点时，新增前，需要显示弹窗让用户选择目标分组，弹窗中把路径展示出来
     * 3.当选中了一个记录节点和一个分组节点时，新增时直接在分组下新增；不可编辑；可以批量删除
     * 4.当选中了多个记录和多个分组时，新增时重复第2点的步骤；且不可编辑；可以批量删除</p>
     *
     * @return 如果允许显示新增操作则返回 {@code true}，否则返回 {@code false}
     * @see HistoryNode
     */
    public boolean isNewOperationVisible(AnActionEvent e) {
        if (null == e.getProject()) return false;

        // 当选中了多个记录节点，或未选中节点的话，不显示新增操作
        List<HistoryNode> selectedNodes = provider.getSelectedRecordNodes();
        if (CollUtil.isEmpty(selectedNodes)) return false;

        // 判断是否都是记录节点
        boolean hasNote = selectedNodes.stream().anyMatch(HistoryNode::isRecord);
        boolean hasGroup = selectedNodes.stream().anyMatch(node -> node.isGroup() || node.isRoot());
        if (hasNote && !hasGroup) {
            long recordCount = selectedNodes.stream().filter(HistoryNode::isRecord).count();
            // 多个记录节点，不显示
            return recordCount == 1;
        }

        return true;
    }

}
