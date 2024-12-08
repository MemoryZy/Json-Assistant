package cn.memoryzy.json.ui;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

public class TreeComparator {

    /**
     * 递归比较两棵树，返回一个包含差异信息的列表。
     * @param tree1 第一个树的根节点
     * @param tree2 第二个树的根节点
     * @return 包含差异信息的列表
     */
    public static List<Difference> compareTrees(DefaultMutableTreeNode tree1, DefaultMutableTreeNode tree2) {
        List<Difference> differences = new ArrayList<>();
        compareNodes(tree1, tree2, differences, ""); // "" 是初始路径
        return differences;
    }

    /**
     * 递归比较两个节点及其子节点。
     * @param node1 第一个节点
     * @param node2 第二个节点
     * @param differences 差异信息列表
     * @param path 节点的路径，用于标识节点位置
     */
    private static void compareNodes(DefaultMutableTreeNode node1, DefaultMutableTreeNode node2, List<Difference> differences, String path) {
        if (node1 == null && node2 == null) return; // 两个节点都为空，没有差异

        if (node1 == null || node2 == null) { // 一个节点为空，另一个不为空
            differences.add(new Difference(path, node1 == null ? node2.getUserObject() : node1.getUserObject(), null));
            return;
        }

        Object obj1 = node1.getUserObject(); // 获取第一个节点的用户对象
        Object obj2 = node2.getUserObject(); // 获取第二个节点的用户对象

        if (!obj1.equals(obj2)) { // 比较两个节点的用户对象是否相同
            differences.add(new Difference(path, obj1, obj2)); // 如果不同，则添加到差异列表
        }

        // 递归比较子节点
        int childCount1 = node1.getChildCount(); // 获取第一个节点的子节点数量
        int childCount2 = node2.getChildCount(); // 获取第二个节点的子节点数量
        int minChildCount = Math.min(childCount1, childCount2); // 获取子节点数量的最小值

        for (int i = 0; i < minChildCount; i++) {
            compareNodes((DefaultMutableTreeNode) node1.getChildAt(i), (DefaultMutableTreeNode) node2.getChildAt(i), differences, path + "/" + i);
        }

        // 处理额外的子节点（一个节点比另一个节点多子节点的情况）
        for (int i = minChildCount; i < childCount1; i++) {
            differences.add(new Difference(path + "/" + i, ((DefaultMutableTreeNode)node1.getChildAt(i)).getUserObject(), null));
        }
        for (int i = minChildCount; i < childCount2; i++) {
            differences.add(new Difference(path + "/" + i, null, ((DefaultMutableTreeNode)node2.getChildAt(i)).getUserObject()));
        }
    }


    /**
     * 表示差异信息的类
     */
    public static class Difference {
        String path; // 节点的路径
        Object value1; // 第一个节点的值
        Object value2; // 第二个节点的值

        public Difference(String path, Object value1, Object value2) {
            this.path = path;
            this.value1 = value1;
            this.value2 = value2;
        }

        @Override
        public String toString() {
            return "Difference{" +
                    "path='" + path + '\'' +
                    ", value1=" + value1 +
                    ", value2=" + value2 +
                    '}';
        }
    }

    public static void main(String[] args) {
        // 示例用法 (请替换为你的实际树创建代码)
        DefaultMutableTreeNode root1 = new DefaultMutableTreeNode("Root");
        root1.add(new DefaultMutableTreeNode("Child1"));
        root1.add(new DefaultMutableTreeNode("Child2"));

        DefaultMutableTreeNode root2 = new DefaultMutableTreeNode("Root");
        root2.add(new DefaultMutableTreeNode("Child1"));
        root2.add(new DefaultMutableTreeNode("Child3"));


        List<Difference> diffs = compareTrees(root1, root2);
        for (Difference diff : diffs) {
            System.out.println(diff);
        }
    }
}









/*

后续
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

public class TreeComparator {

    // ... (compareTrees 方法保持不变) ...

    private static void compareNodes(DefaultMutableTreeNode node1, DefaultMutableTreeNode node2, List<Difference> differences, String path) {
        if (node1 == null && node2 == null) return;

        if (node1 == null) { // node1 为空，差异来自 tree2
            differences.add(new Difference(path, null, node2.getUserObject(), DifferenceType.NODE_ADDED_IN_TREE2));
            return;
        }
        if (node2 == null) { // node2 为空，差异来自 tree1
            differences.add(new Difference(path, node1.getUserObject(), null, DifferenceType.NODE_ADDED_IN_TREE1));
            return;
        }

        Object obj1 = node1.getUserObject();
        Object obj2 = node2.getUserObject();

        if (!obj1.equals(obj2)) { // 值不同
            differences.add(new Difference(path, obj1, obj2, DifferenceType.VALUE_DIFFERENT));
        }

        // ... (子节点比较部分保持不变) ...
    }

    // 定义差异类型枚举
    public enum DifferenceType {
        VALUE_DIFFERENT, // 值不同
        NODE_ADDED_IN_TREE1, // 节点在 tree1 中新增
        NODE_ADDED_IN_TREE2, // 节点在 tree2 中新增
    }


    public static class Difference {
        String path;
        Object value1;
        Object value2;
        DifferenceType type;

        public Difference(String path, Object value1, Object value2, DifferenceType type) {
            this.path = path;
            this.value1 = value1;
            this.value2 = value2;
            this.type = type;
        }

        @Override
        public String toString() {
            return "Difference{" +
                    "path='" + path + '\'' +
                    ", value1=" + value1 +
                    ", value2=" + value2 +
                    ", type=" + type +
                    '}';
        }
    }

    // ... (main 方法保持不变) ...
}


 */