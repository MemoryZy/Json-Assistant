package cn.memoryzy.json.service.persistent.state;

import cn.memoryzy.json.enums.TreeViewMode;
import com.intellij.util.xmlb.annotations.Tag;

/**
 * 树结构配置项
 *
 * @author Memory
 * @since 2025/6/18
 */
@Tag("tree-structure")
public class TreeStructureState {

    /**
     * 树结构展示形式
     */
    private TreeViewMode treeViewMode = TreeViewMode.POPUP;

    /**
     * 是否在树中显示节点的完整路径
     */
    private boolean displayNodePath = true;


    public void setTreeViewMode(TreeViewMode treeViewMode) {
        this.treeViewMode = treeViewMode;
    }

    public void setDisplayNodePath(boolean displayNodePath) {
        this.displayNodePath = displayNodePath;
    }

    public TreeViewMode getTreeViewMode() {
        return treeViewMode;
    }

    public boolean isDisplayNodePath() {
        return displayNodePath;
    }
}
