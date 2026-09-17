package cn.memoryzy.json.ui.tree;

import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.util.Json5Util;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;

/**
 * @author Memory
 * @since 2025/8/27
 */
public class JsonFilterableTree extends FilterableTree<DefaultMutableTreeNode, JsonNode> {

    private JsonWrapper wrapper;

    public JsonFilterableTree(@Nullable Project project, @NotNull JsonNode root, JsonWrapper wrapper) {
        super(project, new Tree(), new DefaultMutableTreeNode(root));
        this.wrapper = wrapper;
        rebuildTree();
    }

    @Override
    protected Class<? extends DefaultMutableTreeNode> getNodeClass() {
        return DefaultMutableTreeNode.class;
    }

    @Override
    protected @NotNull DefaultMutableTreeNode createNode(@NotNull JsonNode node) {
        return new DefaultMutableTreeNode(node);
    }

    @Override
    protected @NotNull Iterable<JsonNode> getChildren(@NotNull JsonNode node) {
        return node.isParentNode() ? node.getChildren() : Collections.emptyList();
    }

    @Override
    protected @Nullable String getText(@Nullable JsonNode node) {
        return null == node ? null : node.toString();
    }

    @Override
    protected void rebuildTree() {
        if (null == wrapper) return;
        JsonNode rootNode = getRootUserObject();
        if (null == rootNode) return;
        // 清空根节点子内容
        rootNode.clear();
        // 组合根节点
        processJsonNode(wrapper, rootNode, "$");
    }

    @Override
    public void update() {
        super.update();
        getSearchModel().updateStructure();
    }

    @Override
    public void updateStructure() {
        super.updateStructure();
        expandMarkedNodes(getTree(), getRoot());
    }

    private void processJsonNode(JsonWrapper jsonWrapper, JsonNode parentNode, String parentPath) {
        if (jsonWrapper instanceof ObjectWrapper) {
            ObjectWrapper jsonObject = (ObjectWrapper) jsonWrapper;
            // 为了确定图标
            if (Objects.isNull(parentNode.getNodeType())) {
                parentNode.setNodeType(JsonTreeNodeType.JSONObject);
            }

            if (Objects.isNull(parentNode.getValue())) {
                parentNode.setValue(jsonObject);
            }

            parentNode.setSize(jsonObject.size());

            // 提取注释Map
            Map<?, ?> commentsMap = Json5Util.getCommentsMap(jsonObject);

            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                // 注释
                if (PluginConstant.COMMENT_KEY.equals(key)) {
                    continue;
                }

                Object value = entry.getValue();
                // 获取注释
                String comment = Json5Util.getComment(commentsMap, key);

                // 构建当前节点路径
                String currentPath = buildCurrentPath(parentPath, key);

                // 构建子节点
                JsonNode childNode = new JsonNode(key)
                        .setComment(comment)
                        .setJsonPath(currentPath)
                        .setParent(parentNode);

                if (value instanceof ObjectWrapper) {
                    ObjectWrapper nestedJsonObject = (ObjectWrapper) value;
                    childNode.setValue(value)
                            .setNodeType(JsonTreeNodeType.JSONObject)
                            .setSize(nestedJsonObject.size());
                    processJsonNode(nestedJsonObject, childNode, currentPath);

                } else if (value instanceof ArrayWrapper) {
                    ArrayWrapper jsonArray = (ArrayWrapper) value;
                    childNode.setValue(value)
                            .setNodeType(JsonTreeNodeType.JSONArray)
                            .setSize(jsonArray.size());
                    processArrayChildren(childNode, jsonArray, currentPath);

                } else {
                    // 若不是对象或数组，则不添加子集，直接同层级
                    childNode.setValue(value)
                            .setNodeType(JsonTreeNodeType.JSONObjectProperty);
                }

                parentNode.add(childNode);
            }
        } else if (jsonWrapper instanceof ArrayWrapper) {
            ArrayWrapper jsonArray = (ArrayWrapper) jsonWrapper;
            // 为了确定图标
            if (Objects.isNull(parentNode.getNodeType())) {
                parentNode.setNodeType(JsonTreeNodeType.JSONArray);
            }

            if (Objects.isNull(parentNode.getSize())) {
                parentNode.setSize(jsonArray.size());
            }

            if (Objects.isNull(parentNode.getValue())) {
                parentNode.setValue(jsonArray);
            }

            processArrayChildren(parentNode, jsonArray, parentPath);
        }
    }

    private void processArrayChildren(JsonNode parentNode, ArrayWrapper jsonArray, String parentPath) {
        for (int i = 0; i < jsonArray.size(); i++) {
            Object element = jsonArray.get(i);

            // 构建当前节点路径
            String currentPath = buildArrayElementPath(parentPath, i);

            if (element instanceof ObjectWrapper) {
                ObjectWrapper jsonObjectElement = (ObjectWrapper) element;
                JsonNode childNode = new JsonNode("item" + i)
                        .setValue(element)
                        .setNodeType(JsonTreeNodeType.JSONObjectElement)
                        .setSize(jsonObjectElement.size())
                        .setJsonPath(currentPath)
                        .setParent(parentNode);

                processJsonNode(jsonObjectElement, childNode, currentPath);
                parentNode.add(childNode);

            } else if (element instanceof ArrayWrapper) {
                ArrayWrapper jsonArrayElement = (ArrayWrapper) element;
                JsonNode childNodeElement = new JsonNode("item" + i)
                        .setValue(element)
                        .setNodeType(JsonTreeNodeType.JSONArrayElementArray)
                        .setSize(jsonArrayElement.size())
                        .setJsonPath(currentPath)
                        .setParent(parentNode);

                processJsonNode(jsonArrayElement, childNodeElement, currentPath);
                parentNode.add(childNodeElement);
            } else {
                Object obj = element;
                if (element instanceof String) {
                    String str = (String) element;
                    obj = "\"" + str + "\"";
                }

                JsonNode childNode = new JsonNode(obj)
                        .setValue(element)
                        .setNodeType(JsonTreeNodeType.JSONArrayElement)
                        .setJsonPath(currentPath)
                        .setParent(parentNode);

                parentNode.add(childNode);
            }
        }
    }

    /**
     * 为对象属性构建路径
     */
    public static String buildCurrentPath(String parentPath, String key) {
        if (parentPath.isEmpty() || "$".equals(parentPath)) {
            return "$." + key;
        }
        return parentPath + "." + key;
    }

    /**
     * 为数组元素构建路径
     */
    public static String buildArrayElementPath(String parentPath, int index) {
        return parentPath + "[" + index + "]";
    }

    public static JsonNode getNode(TreePath path) {
        if (null == path) return null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        return (JsonNode) node.getUserObject();
    }

    /**
     * 递归展开标记为需要展开的节点
     */
    public static void expandMarkedNodes(JTree tree, DefaultMutableTreeNode root) {
        Enumeration<TreeNode> nodes = root.depthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();

            // 只处理非叶子节点
            if (!node.isLeaf()) {
                JsonNode data = (JsonNode) node.getUserObject();

                if (data.isExpanded()) {
                    TreePath path = new TreePath(node.getPath());
                    tree.expandPath(path);
                }
            }
        }
    }

    public JsonFilterableTree setWrapper(JsonWrapper wrapper) {
        this.wrapper = wrapper;
        return this;
    }
}
