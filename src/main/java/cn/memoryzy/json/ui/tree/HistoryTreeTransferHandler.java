package cn.memoryzy.json.ui.tree;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.HistoryAffectType;
import cn.memoryzy.json.event.RefreshHistoryTreeEvent;
import cn.memoryzy.json.service.persistent.HistoryManager;
import cn.memoryzy.json.service.persistent.state.JsonGroup;
import cn.memoryzy.json.service.persistent.state.JsonRecord;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import cn.memoryzy.json.util.Notifications;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 历史记录树的拖拽处理器。
 * <p>
 * 历史记录树的结构由持久化的数据模型决定：分组之间通过 {@code JsonGroup.parentId} 嵌套，
 * 记录通过 {@code JsonGroup.recordIds} 挂载到分组（不挂在任何分组上的记录即为“孤立记录”，展示在根节点下）。
 * 因此“拖拽”本质上不是移动 Swing 节点，而是修改数据模型中的归属关系，再触发一次树重建。
 * <p>
 * 支持的拖拽语义：
 * <ul>
 *     <li>拖到「分组」上：记录 / 分组移动进该分组</li>
 *     <li>拖到「记录」上：视为拖到该记录所在的分组（顶层记录则为根节点）</li>
 *     <li>拖到「根节点」上：记录 / 分组回到顶层</li>
 * </ul>
 * 出于以下两点，目标只允许是节点本身（{@code DropMode.ON}），不支持“插入到兄弟节点之间”：
 * 第一，树的排序是按“类型 + 名称 + 更新时间”重新计算的，插入位置无法持久化；
 * 第二，数据模型里也没有同级顺序字段。
 * <p>
 * 注意：分组被移动时，其子孙分组与记录会一起被移动（因为它们仍指向该分组），
 * 这符合用户在树上看到的“整棵子树跟着走”的直觉。
 *
 * @author Memory
 * @since 2025/12/19
 */
public class HistoryTreeTransferHandler extends TransferHandler {

    /**
     * 拖拽数据风味（Flavor）。
     * <p>
     * 这是 JVM 内部对象风味，只在当前 JVM 内有效，因此不会与系统剪贴板、外部程序的拖拽产生冲突：
     * 历史记录树只接受“本插件内部拖出来的节点列表”。
     */
    private static final DataFlavor NODE_LIST_FLAVOR = createNodeListFlavor();

    /**
     * 拖拽失败提示：目标位置已存在同名项
     */
    private static final String ERROR_DUPLICATE_NAME_KEY = "history.tree.drag.error.duplicate.name";

    /**
     * 拖拽失败提示：分组不能移动到自身或自身的子分组中
     */
    private static final String ERROR_INVALID_TARGET_KEY = "history.tree.drag.error.invalid.target";

    /**
     * 工具窗口 Provider，用于取树、取节点对应的 {@link HistoryNode}，以及取项目级服务
     */
    private final HistoryToolWindowComponentProvider provider;

    /**
     * 当前项目（历史记录是按项目维度持久化的，因此刷新事件也按项目维度发送）
     */
    private final Project project;

    /**
     * 历史记录管理器，负责分组/记录的归属关系与持久化
     */
    private final HistoryManager historyManager;

    public HistoryTreeTransferHandler(@NotNull HistoryToolWindowComponentProvider provider) {
        this.provider = provider;
        this.project = provider.getProject();
        this.historyManager = provider.getHistoryManager();
    }

    // ---------------------------------- 拖拽数据准备 ---------------------------------- //

    /**
     * 创建可拖拽的数据。
     * <p>
     * 只在树上有“非根节点”被选中时才允许拖拽；根节点代表整个“全部记录”，不能拖动。
     */
    @Override
    protected @Nullable Transferable createTransferable(JComponent component) {
        if (!(component instanceof Tree)) {
            return null;
        }

        Tree tree = (Tree) component;
        List<HistoryNode> nodes = collectDraggableNodes(tree.getSelectionPaths());
        if (nodes.isEmpty()) {
            return null;
        }

        return new HistoryNodesTransferable(nodes);
    }

    /**
     * 只允许“移动”，不复制：历史记录是同一份数据的归属变化，复制没有意义。
     */
    @Override
    public int getSourceActions(JComponent component) {
        return MOVE;
    }

    // ---------------------------------- 拖拽放置 ---------------------------------- //

    /**
     * 是否接受拖放。
     * <p>
     * 这里只做“数据类型”的粗粒度校验，具体的目标合法性（是否成环、是否重名）在 {@link #importData} 中校验，
     * 因为 {@code canImport} 阶段拿不到 {@link JTree.DropLocation}。
     */
    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDrop() && support.isDataFlavorSupported(NODE_LIST_FLAVOR);
    }

    /**
     * 处理拖放：把节点从原来的容器（分组或根节点）移动到目标容器。
     *
     * @return 是否真的发生了移动；返回 {@code false} 时 Swing 会认为放置失败（节点保持原样）
     */
    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support) || !(support.getComponent() instanceof Tree)) {
            return false;
        }

        // 目标位置：DropMode.ON 下为用户放开的那个节点；在空白处放开时为 null，直接忽略
        JTree.DropLocation location = (JTree.DropLocation) support.getDropLocation();
        if (location == null || location.getPath() == null) {
            return false;
        }

        List<HistoryNode> movedNodes = readTransferData(support);
        if (movedNodes == null || movedNodes.isEmpty()) {
            return false;
        }

        DefaultMutableTreeNode dropTreeNode = (DefaultMutableTreeNode) location.getPath().getLastPathComponent();
        HistoryNode dropNode = provider.getFilterableTree().getUserObject(dropTreeNode);
        if (dropNode == null) {
            return false;
        }

        // 计算目标容器：目标分组（为 null 表示根节点/顶层）
        JsonGroup targetGroup;
        if (dropNode.isRoot()) {
            // 拖到根节点：回到顶层
            targetGroup = null;
        } else if (dropNode.isGroup()) {
            // 拖到分组上：放入该分组
            targetGroup = (JsonGroup) dropNode.getValue();
        } else {
            // 拖到记录上：放进该记录所在的分组（记录在顶层时，目标就是根节点）
            DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode) dropTreeNode.getParent();
            HistoryNode containerNode = parentTreeNode == null ? null : provider.getFilterableTree().getUserObject(parentTreeNode);
            targetGroup = (containerNode == null || containerNode.isRoot()) ? null : (JsonGroup) containerNode.getValue();
        }

        String targetGroupId = targetGroup == null ? null : targetGroup.getId();

        // 校验一：分组不能移动到自己或自己的子孙分组里，否则会形成环（树重建时该分组及其子树会整体丢失）
        if (!isTargetGroupLegal(movedNodes, targetGroup)) {
            notifyWarning(ERROR_INVALID_TARGET_KEY);
            return false;
        }

        // 校验二：同一容器内不能出现同名项（与新建/重命名时的校验规则保持一致：分组与记录之间允许同名）
        if (hasNameConflict(movedNodes, targetGroup)) {
            notifyWarning(ERROR_DUPLICATE_NAME_KEY);
            return false;
        }

        // 应用归属变更
        int changedCount = 0;
        for (HistoryNode node : movedNodes) {
            if (node.isRecord()) {
                JsonRecord record = (JsonRecord) node.getValue();
                // 已经在该容器里，无需处理
                if (Objects.equals(record.getParentId(), targetGroupId)) continue;

                // 记录在旧版数据里可能被多个分组重复挂载，这里全部解除，保证数据一致
                detachRecordFromAllGroups(record);
                record.setParentId(targetGroupId);
                if (targetGroupId != null) {
                    historyManager.associateRecordWithGroup(targetGroupId, record.getId());
                }
                changedCount++;
            } else if (node.isGroup()) {
                JsonGroup group = (JsonGroup) node.getValue();
                // 已经在该容器里，无需处理
                if (Objects.equals(group.getParentId(), targetGroupId)) continue;

                // 分组只改父级，其内部的记录/子分组会跟着整体移动
                group.setParentId(targetGroupId);
                changedCount++;
            }
        }

        // 没有任何实际变化（例如把节点原样拖回原位），不写文件也不刷新
        if (changedCount == 0) {
            return false;
        }

        // 持久化到 histories.json
        historyManager.saveState();

        // 刷新树：使用 MOVE 类型，刷新逻辑会在重建后重新选中被拖动的节点
        // 与新建分组/新建记录一致，通过应用级消息总线广播，保证所有打开的项目窗口都能刷新
        ApplicationManager.getApplication().getMessageBus()
                .syncPublisher(RefreshHistoryTreeEvent.TOPIC)
                .refresh(project, HistoryAffectType.MOVE, movedNodes.get(0).getId());
        return true;
    }

    // ---------------------------------- 内部逻辑 ---------------------------------- //

    /**
     * 从当前选中路径中收集可拖拽的节点。
     * <p>
     * 做了两件事：过滤掉根节点；过滤掉“祖先也被选中”的节点（祖先移动时子孙会一起移动，重复处理没有意义）。
     */
    @NotNull
    private List<HistoryNode> collectDraggableNodes(@Nullable TreePath[] selectionPaths) {
        List<HistoryNode> nodes = new ArrayList<>();
        if (selectionPaths == null) {
            return nodes;
        }

        for (TreePath path : selectionPaths) {
            HistoryNode node = provider.getFilterableTree().getUserObject(path);
            // 根节点不可拖动；记录/分组之外的节点也一并忽略
            if (node == null || node.isRoot() || (!node.isGroup() && !node.isRecord())) {
                continue;
            }
            nodes.add(node);
        }

        // 过滤掉子孙节点：只要某个节点的祖先已经在待移动列表中，就跳过该节点
        List<HistoryNode> result = new ArrayList<>(nodes.size());
        for (HistoryNode node : nodes) {
            if (!hasSelectedAncestor(node, nodes)) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * 判断某个节点的祖先是否也在待移动列表中（使用 {@link HistoryNode#getParentNode()} 的当前树结构关系）
     */
    private boolean hasSelectedAncestor(@NotNull HistoryNode node, @NotNull List<HistoryNode> nodes) {
        HistoryNode parent = node.getParentNode();
        while (parent != null && parent.isNotRoot()) {
            if (isSameNode(parent, nodes)) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }

    /**
     * 判断节点是否与列表中的某个节点是同一个（用 ID 比较，因为刷新后 {@link HistoryNode} 对象会被重建）
     */
    private boolean isSameNode(@NotNull HistoryNode node, @NotNull List<HistoryNode> nodes) {
        String id = node.getId();
        if (id == null) {
            return false;
        }
        for (HistoryNode candidate : nodes) {
            if (Objects.equals(id, candidate.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 读取拖拽数据；数据风味不匹配时返回 {@code null}
     */
    @Nullable
    @SuppressWarnings("unchecked")
    private List<HistoryNode> readTransferData(@NotNull TransferSupport support) {
        try {
            Object data = support.getTransferable().getTransferData(NODE_LIST_FLAVOR);
            return data instanceof List ? (List<HistoryNode>) data : null;
        } catch (UnsupportedFlavorException | IOException e) {
            // 属于拖拽数据异常，忽略这次放置即可，无需提示用户
            return null;
        }
    }

    /**
     * 校验目标分组是否合法：被移动的分组不能落到自己或自己的子孙分组下。
     *
     * @param movedNodes  被拖动的节点
     * @param targetGroup 目标分组，{@code null} 表示顶层
     */
    private boolean isTargetGroupLegal(@NotNull List<HistoryNode> movedNodes, @Nullable JsonGroup targetGroup) {
        for (HistoryNode node : movedNodes) {
            if (!node.isGroup()) continue;

            String movingGroupId = node.getId();
            // 沿着目标分组的父级链向上找，一旦找到被移动的分组，说明会形成环
            JsonGroup cursor = targetGroup;
            while (cursor != null) {
                if (Objects.equals(cursor.getId(), movingGroupId)) {
                    return false;
                }
                cursor = historyManager.findGroup(cursor.getParentId());
            }
        }
        return true;
    }

    /**
     * 校验目标容器内是否已经存在同名项。
     * <p>
     * 这里直接查询持久化模型（而不是遍历当前树节点），因为树可能正处于搜索过滤状态，
     * 被过滤掉的同级节点同样会造成重名。分组与记录分开比较，与新建/重命名逻辑保持一致：
     * 分组与记录之间允许同名，同类型之间不允许同名。
     *
     * @param movedNodes  被拖动的节点
     * @param targetGroup 目标分组，{@code null} 表示顶层
     */
    private boolean hasNameConflict(@NotNull List<HistoryNode> movedNodes, @Nullable JsonGroup targetGroup) {
        String targetGroupId = targetGroup == null ? null : targetGroup.getId();

        // 目标容器内已有的分组（parentId 为 null 时 findChildGroups 返回的就是顶层分组）
        List<JsonGroup> siblingGroups = historyManager.findChildGroups(targetGroupId);
        // 目标容器内已有的记录：顶层记录与分组下的记录分别取
        List<JsonRecord> siblingRecords = targetGroup == null
                ? historyManager.findTopLevelRecords()
                : historyManager.findRecords(targetGroup.getRecordIds());

        for (HistoryNode movedNode : movedNodes) {
            String movedName = movedNode.getNodeName();
            // 无名项（名称为空白）不参与重名校验
            if (movedName == null || movedName.trim().isEmpty()) {
                continue;
            }

            if (movedNode.isGroup()) {
                for (JsonGroup sibling : siblingGroups) {
                    // 自己不算冲突（层级没变时属于原容器）
                    if (Objects.equals(sibling.getId(), movedNode.getId())) continue;
                    if (movedName.equals(sibling.getName())) {
                        return true;
                    }
                }
            } else if (movedNode.isRecord()) {
                for (JsonRecord sibling : siblingRecords) {
                    if (Objects.equals(sibling.getId(), movedNode.getId())) continue;
                    // 记录名称为空时，树上展示的是短文本，因此按同样的规则取展示名比较
                    if (movedName.equals(recordDisplayName(sibling))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 取记录的展示名称：名称非空时用名称，否则用短文本（与 {@code HistoryFilterableTree} 的展示规则一致）
     */
    @NotNull
    private String recordDisplayName(@NotNull JsonRecord record) {
        String name = record.getName();
        return name == null || name.trim().isEmpty() ? Objects.toString(record.getDisplayText(), "") : name;
    }

    /**
     * 把记录从所有分组的 {@code recordIds} 中解除绑定。
     * <p>
     * 正常数据里一份记录只会属于一个分组，这里遍历所有分组是为了兜底：即使历史文件里出现脏数据，
     * 拖拽之后也能收敛成“一份记录只属于一个容器”。
     */
    private void detachRecordFromAllGroups(@NotNull JsonRecord record) {
        String recordId = record.getId();
        for (JsonGroup group : historyManager.getGroups()) {
            if (group.getRecordIds().contains(recordId)) {
                historyManager.detachRecordFromGroup(group, recordId);
            }
        }
    }

    /**
     * 以警告气泡提示拖拽失败的原因
     */
    private void notifyWarning(@NotNull String messageKey) {
        Notifications.showNotification(JsonAssistantBundle.messageOnSystem(messageKey), NotificationType.WARNING, project);
    }

    /**
     * 创建拖拽数据风味：JVM 内部对象风味，承载 {@code List<HistoryNode>}
     */
    @NotNull
    private static DataFlavor createNodeListFlavor() {
        return new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + List.class.getName(), "JsonAssistant History Nodes");
    }

    /**
     * 拖拽数据的载体：只包装一个节点列表
     */
    private static class HistoryNodesTransferable implements Transferable {

        /**
         * 被拖动的节点（处于拖拽发生时的树结构快照中）
         */
        private final List<HistoryNode> nodes;

        HistoryNodesTransferable(@NotNull List<HistoryNode> nodes) {
            this.nodes = nodes;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{NODE_LIST_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return NODE_LIST_FLAVOR.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return nodes;
        }
    }
}
