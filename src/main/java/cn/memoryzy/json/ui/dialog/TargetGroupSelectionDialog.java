package cn.memoryzy.json.ui.dialog;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.tree.GroupSelectionNode;
import cn.memoryzy.json.ui.tree.HistoryNode;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

/**
 * @author Memory
 * @since 2025/9/15
 */
public class TargetGroupSelectionDialog extends DialogWrapper {

    private final Tree tree;

    public TargetGroupSelectionDialog(@Nullable Project project, List<HistoryNode> groupNodes) {
        super(project, true);
        this.tree = new Tree(createStructure(groupNodes));

        getOKAction().setEnabled(false);
        setTitle(JsonAssistantBundle.messageOnSystem("dialog.select.targetGroup.title"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        TreeUtil.installActions(tree);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> updateOkButtonState());

        Font font = UIUtils.JETBRAINS_MAPLE_MONO_FONT;
        if (null == font) {
            // 先设置 JetBrains Mono，在搜索时，切换为
            font = UIUtils.jetBrainsMonoFont(JBUIScale.scaleFontSize(12));
        }

        tree.setFont(font);
        tree.setExpandableItemsEnabled(true);
        tree.setCellRenderer(new NodeRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                GroupSelectionNode node = (GroupSelectionNode) value;
                setIcon(node.getIcon());
                append(node.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        });

        new TreeSpeedSearch(tree);

        UIUtils.selectFirstLeafNode(tree);

        BorderLayoutPanel panel = new BorderLayoutPanel().addToCenter(UIUtils.wrapScrollPane(tree));
        panel.setPreferredSize(new Dimension(350, 160));
        return panel;
    }

    /**
     * 更新OK按钮状态的辅助方法
     */
    private void updateOkButtonState() {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath == null) {
            // 没有选中任何节点，禁用OK按钮
            getOKAction().setEnabled(false);
            return;
        }

        // 获取选中的节点
        Object lastPathComponent = selectionPath.getLastPathComponent();
        if (lastPathComponent instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) lastPathComponent;
            // 根据是否是叶子节点来启用或禁用OK按钮
            getOKAction().setEnabled(selectedNode.isLeaf());
        }
    }

    private GroupSelectionNode createStructure(List<HistoryNode> groupNodes) {
        GroupSelectionNode rootNode = new GroupSelectionNode(JsonAssistantBundle.messageOnSystem("toolwindow.history.tree.root.text"))
                .setIcon(AllIcons.Actions.ModuleDirectory)
                .setDepth(0);

        // 根节点排序要在其他节点前；路径少的节点排序要在路径多的节点前
        groupNodes.sort(Comparator
                // 规则 1 & 2: 根节点优先，然后路径层级少的优先
                .comparingInt((HistoryNode node) -> {
                    String path = node.getPath();
                    // 判断是否为根节点 (不包含 '/')
                    if (!path.contains("/")) {
                        return Integer.MIN_VALUE; // 赋予根节点最小的值，使其排在最前
                    }
                    // 非根节点：计算路径深度 ('/' 出现的次数)
                    return JsonAssistantUtil.countCharacterOccurrences(path, '/');
                })
                // 规则 3: 如果路径层级相同（比较函数返回值相等），则按 path 字符串的自然顺序排序
                .thenComparing(HistoryNode::getPath));

        for (HistoryNode historyNode : groupNodes) {
            String path = historyNode.getPath();
            if (StrUtil.isBlank(path)) continue;

            if (path.contains("/")) {
                // 截取掉第一段根节点的路径
                path = path.replaceFirst(".*?/", "…/").replace("/", "\\");
            } else {
                // 把根节点替换为…
                path = "…";
            }

            rootNode.add(new GroupSelectionNode(path).setIcon(AllIcons.Nodes.Folder).setOriginalNode(historyNode));
        }

        return rootNode;
    }

    public HistoryNode getSelectedNode() {
        GroupSelectionNode node = getUserObject(tree.getSelectionPath());
        return null == node ? null : node.getOriginalNode();
    }

    private GroupSelectionNode getUserObject(TreePath path) {
        if (null == path) return null;
        return (GroupSelectionNode) path.getLastPathComponent();
    }

}
