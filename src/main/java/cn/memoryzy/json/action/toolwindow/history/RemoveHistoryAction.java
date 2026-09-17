package cn.memoryzy.json.action.toolwindow.history;

import cn.hutool.core.collection.CollUtil;
import cn.memoryzy.json.action.DumbAwareBaseAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.HistoryAffectType;
import cn.memoryzy.json.event.RefreshHistoryTreeEvent;
import cn.memoryzy.json.service.persistent.HistoryManager;
import cn.memoryzy.json.service.persistent.state.JsonGroup;
import cn.memoryzy.json.service.persistent.state.JsonRecord;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import cn.memoryzy.json.ui.tree.HistoryNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2025/8/18
 */
public class RemoveHistoryAction extends DumbAwareBaseAction {

    private final HistoryToolWindowComponentProvider provider;

    public RemoveHistoryAction(HistoryToolWindowComponentProvider provider, SimpleToolWindowPanel windowPanel) {
        super(JsonAssistantBundle.messageOnSystem("action.remove.history.text"), null, IconUtil.getRemoveIcon());
        registerCustomShortcutSet(CustomShortcutSet.fromString("DELETE"), windowPanel);
        this.provider = provider;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (null == project) return;

        List<HistoryNode> selectedNodes = provider.getSelectedRecordNodes();

        boolean isSingleRecord = false;
        boolean isSingleGroup = false;
        boolean isMultiRecord = false;
        boolean isMultiGroup = false;
        boolean isMultiMix = false;
        boolean isAll = false;

        String message;
        // 删除所有记录
        if (selectedNodes.stream().anyMatch(HistoryNode::isRoot)) {
            // 只要包含了根节点，就表示要全部删除
            isAll = true;
            message = JsonAssistantBundle.messageOnSystem("dialog.remove.history.all.text");

        } else {
            // 单个删除记录 / 分组
            if (selectedNodes.size() == 1) {
                HistoryNode node = selectedNodes.get(0);
                if (node.isRecord()) {
                    isSingleRecord = true;
                    message = JsonAssistantBundle.messageOnSystem("dialog.remove.history.single.record.text", node.getNodeName());
                } else {
                    isSingleGroup = true;
                    message = JsonAssistantBundle.messageOnSystem("dialog.remove.history.single.group.text", node.getNodeName());
                }

            } else {
                if (selectedNodes.stream().allMatch(HistoryNode::isRecord)) {
                    // 删除多记录
                    isMultiRecord = true;
                    message = JsonAssistantBundle.messageOnSystem("dialog.remove.history.multi.record.text", selectedNodes.size());

                } else if (selectedNodes.stream().allMatch(HistoryNode::isGroup)) {
                    // 删除多分组
                    isMultiGroup = true;
                    message = JsonAssistantBundle.messageOnSystem("dialog.remove.history.multi.group.text", selectedNodes.size());

                } else {
                    // 有记录有分组
                    int groupNum = 0;
                    int recordNum = 0;
                    for (HistoryNode selectedNode : selectedNodes) {
                        if (selectedNode.isGroup()) {
                            groupNum++;
                        } else {
                            recordNum++;
                        }
                    }

                    isMultiMix = true;
                    message = JsonAssistantBundle.messageOnSystem("dialog.remove.history.multi.mix.text", groupNum, recordNum);
                }
            }
        }

        HistoryManager historyManager = HistoryManager.getInstance(project);
        List<HistoryNode> removeNodes = new ArrayList<>();
        if (Messages.OK == Messages.showOkCancelDialog(message, JsonAssistantBundle.messageOnSystem("dialog.remove.history.title"), JsonAssistantBundle.messageOnSystem("dialog.ok.text"), JsonAssistantBundle.messageOnSystem("dialog.cancel.text"), Messages.getQuestionIcon())) {
            if (isSingleRecord) {
                HistoryNode historyNode = selectedNodes.get(0);
                removeNodes.add(historyNode);
                historyManager.delRecord((JsonRecord) historyNode.getValue());

            } else if (isSingleGroup) {
                HistoryNode groupNode = selectedNodes.get(0);
                removeNodes.addAll(groupNode.getChildren());
                historyManager.delGroup((JsonGroup) groupNode.getValue());

            } else if (isMultiRecord) {
                removeNodes.addAll(selectedNodes);
                historyManager.delRecords(selectedNodes.stream().map(node -> node.getValue().getId()).collect(Collectors.toList()));

            } else if (isMultiGroup) {
                selectedNodes.forEach(group -> removeNodes.addAll(group.getChildren()));
                historyManager.delGroups(selectedNodes.stream().map(group -> group.getValue().getId()).collect(Collectors.toList()));

            } else if (isMultiMix) {
                List<String> recordIds = new ArrayList<>();
                List<String> groupIds = new ArrayList<>();

                for (HistoryNode selectedNode : selectedNodes) {
                    if (selectedNode.isGroup()) {
                        removeNodes.addAll(selectedNode.getChildren());
                        groupIds.add(selectedNode.getValue().getId());
                    } else {
                        removeNodes.add(selectedNode);
                        recordIds.add(selectedNode.getValue().getId());
                    }
                }

                historyManager.delRecords(recordIds, false);
                historyManager.delGroups(groupIds, true);

            } else {
                historyManager.delAllGroups();
            }

            // 通过消息机制可以刷新所有项目的树
            ApplicationManager.getApplication().getMessageBus()
                    .syncPublisher(RefreshHistoryTreeEvent.TOPIC)
                    .refresh(getEventProject(e), HistoryAffectType.REMOVE, null);

            // 清除释放对应的编辑器
            if (isAll) {
                provider.releaseAllEditorImmediately();
            } else {
                provider.releaseEditorImmediately(removeNodes);
            }
        }
    }

    @Override
    protected boolean isActionEnabled(@NotNull AnActionEvent e) {
        return super.isActionEnabled(e) && CollUtil.isNotEmpty(provider.getSelectedNodes());
    }
}
