package cn.memoryzy.json.ui;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.memoryzy.json.actions.child.*;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeValueTypeEnum;
import cn.memoryzy.json.icons.JsonAssistantIcons;
import cn.memoryzy.json.ui.treenode.JsonCollectInfoMutableTreeNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.*;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/3/7
 */
public class JsonStructureWindow extends DialogWrapper {

    private Tree tree;
    private final JSON json;

    public JsonStructureWindow(@Nullable Project project, JSON json) {
        super(project, true);
        this.json = json;

        setModal(false);
        setTitle(JsonAssistantBundle.message("json.structure.window.title"));
        setOKButtonText(JsonAssistantBundle.message("ok.button.text"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JsonCollectInfoMutableTreeNode rootNode = new JsonCollectInfoMutableTreeNode("root");
        convertToTreeNode(json, rootNode);
        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        // 构建树
        tree = new Tree(model);
        tree.setDragEnabled(true);
        tree.setExpandableItemsEnabled(true);
        tree.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));

        initCellRenderer();
        initPopupMenu();
        // 触发快速检索
        new TreeSpeedSearch(tree);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(tree)
                .addExtraAction(new ExpandAllAction(tree))
                .addExtraAction(new CollapseAllAction(tree));

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(decorator.createPanel(), BorderLayout.CENTER);
        rootPanel.setPreferredSize(new Dimension(400, 470));

        return rootPanel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        List<Action> actions = new ArrayList<>();
        actions.add(getOKAction());
        return actions.toArray(new Action[0]);
    }


    public void convertToTreeNode(JSON json, JsonCollectInfoMutableTreeNode node) {
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            // 为了确定图标
            if (Objects.isNull(node.getValueType())) {
                node.setValueType(JsonTreeNodeValueTypeEnum.JSONObject);
            }

            if (Objects.isNull(node.getCorrespondingValue())) {
                node.setCorrespondingValue(jsonObject);
            }

            node.setSize(jsonObject.size());

            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                JsonCollectInfoMutableTreeNode childNode = new JsonCollectInfoMutableTreeNode(key);

                if (value instanceof JSONObject) {
                    JSONObject jsonObjectValue = (JSONObject) value;
                    childNode.setCorrespondingValue(value).setValueType(JsonTreeNodeValueTypeEnum.JSONObject).setSize(jsonObjectValue.size());
                    convertToTreeNode(jsonObjectValue, childNode);

                } else if (value instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) value;
                    childNode.setCorrespondingValue(value).setValueType(JsonTreeNodeValueTypeEnum.JSONArray).setSize(jsonArray.size());
                    for (int i = 0; i < jsonArray.size(); i++) {
                        Object el = jsonArray.get(i);
                        if (el instanceof JSONObject) {
                            JSONObject jsonObjectEl = (JSONObject) el;
                            JsonCollectInfoMutableTreeNode childNodeEl = new JsonCollectInfoMutableTreeNode(
                                    "item" + i,
                                    el,
                                    JsonTreeNodeValueTypeEnum.JSONObjectEl,
                                    jsonObjectEl.size());

                            convertToTreeNode(jsonObjectEl, childNodeEl);
                            childNode.add(childNodeEl);
                        } else {
                            Object obj = el;
                            if (el instanceof String) {
                                String str = (String) el;
                                obj = "\"" + str + "\"";
                            }

                            childNode.add(new JsonCollectInfoMutableTreeNode(obj).setValueType(JsonTreeNodeValueTypeEnum.JSONArrayEl));
                        }
                    }

                } else {
                    // 若不是对象或数组，则不添加子集，直接同层级
                    if (value instanceof JSONNull) {
                        value = null;
                    }

                    childNode.setCorrespondingValue(value)
                            .setValueType(JsonTreeNodeValueTypeEnum.JSONObjectKey)
                            .setUserObject(key);
                }

                node.add(childNode);
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            // 为了确定图标
            node.setValueType(JsonTreeNodeValueTypeEnum.JSONArray).setSize(jsonArray.size());
            if (Objects.isNull(node.getCorrespondingValue())) {
                node.setCorrespondingValue(jsonArray);
            }

            for (int i = 0; i < jsonArray.size(); i++) {
                Object el = jsonArray.get(i);
                if (el instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) el;
                    JsonCollectInfoMutableTreeNode childNode = new JsonCollectInfoMutableTreeNode("item" + i, el, JsonTreeNodeValueTypeEnum.JSONObjectEl, jsonObject.size());
                    convertToTreeNode(jsonObject, childNode);
                    node.add(childNode);
                } else {
                    Object obj = el;
                    if (el instanceof String) {
                        String str = (String) el;
                        obj = "\"" + str + "\"";
                    }

                    node.add(new JsonCollectInfoMutableTreeNode(obj).setValueType(JsonTreeNodeValueTypeEnum.JSONArrayEl));
                }
            }
        }
    }


    private void initCellRenderer() {
        tree.setCellRenderer(new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JsonCollectInfoMutableTreeNode jsonCollectInfoMutableTreeNode = (JsonCollectInfoMutableTreeNode) value;
                JsonTreeNodeValueTypeEnum nodeValueType = jsonCollectInfoMutableTreeNode.getValueType();

                String text = jsonCollectInfoMutableTreeNode.getUserObject().toString();
                SimpleTextAttributes simpleTextAttributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
                append(Objects.equals(JsonTreeNodeValueTypeEnum.JSONObjectKey, nodeValueType) ? text + ": " : text, simpleTextAttributes);

                SimpleTextAttributes lightAttributes = SimpleTextAttributes.merge(simpleTextAttributes, SimpleTextAttributes.GRAYED_ATTRIBUTES);
                SimpleTextAttributes blueAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(63, 120, 230), new Color(137, 174, 246)));
                SimpleTextAttributes purpleAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(248, 108, 101), new Color(244, 184, 181)));

                SimpleTextAttributes stringColorAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(6, 125, 23), new Color(104, 169, 114)));
                SimpleTextAttributes booleanWithNullColorAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(0, 51, 179), new Color(206, 141, 108)));
                SimpleTextAttributes numberColorAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(25, 80, 234), new Color(41, 171, 183)));

                Icon icon = JsonAssistantIcons.Structure.JSON_KEY;

                Integer size = jsonCollectInfoMutableTreeNode.getSize();
                Object correspondingValue = jsonCollectInfoMutableTreeNode.getCorrespondingValue();
                JsonTreeNodeValueTypeEnum valueType = jsonCollectInfoMutableTreeNode.getValueType();

                String squareBracketsStart = "";
                String nodeTypeStr = "";
                String squareBracketsEnd = "";

                String sizeStrPre = "";
                String sizeStr = "";
                String sizeStrPost = "";

                // json的value
                String jsonValue = "";
                String jsonValueType = "";

                if (Objects.nonNull(valueType)) {
                    switch (valueType) {
                        case JSONObject: {
                            squareBracketsStart = " [";
                            nodeTypeStr = "object";
                            squareBracketsEnd = "]";
                            sizeStrPre = " (";
                            sizeStr = size + " " + JsonAssistantBundle.message("json.structure.window.size.obj.text");
                            sizeStrPost = ")";

                            icon = JsonAssistantIcons.Structure.JSON_OBJECT;
                            break;
                        }

                        case JSONArray: {
                            squareBracketsStart = " [";
                            nodeTypeStr = "array";
                            squareBracketsEnd = "]";
                            sizeStrPre = " (";
                            sizeStr = size + " " + JsonAssistantBundle.message("json.structure.window.size.array.text");
                            sizeStrPost = ")";

                            icon = JsonAssistantIcons.Structure.JSON_ARRAY;
                            break;
                        }

                        case JSONObjectEl: {
                            squareBracketsStart = " [";
                            nodeTypeStr = "array_object";
                            squareBracketsEnd = "]";
                            sizeStrPre = " (";
                            sizeStr = size + " " + JsonAssistantBundle.message("json.structure.window.size.obj.text");
                            sizeStrPost = ")";

                            icon = JsonAssistantIcons.Structure.JSON_OBJECT_ITEM;
                            break;
                        }

                        case JSONArrayEl: {
                            icon = JsonAssistantIcons.Structure.JSON_ITEM;
                            break;
                        }

                        case JSONObjectKey: {
                            String valueStr;
                            if (Objects.isNull(correspondingValue)) {
                                valueStr = "null";
                                jsonValueType = "null";
                            } else {
                                if (correspondingValue instanceof String) {
                                    String str = (String) correspondingValue;

                                    if (str.isEmpty()) {
                                        valueStr = "\"\"";
                                    } else {
                                        valueStr = "\"" + str + "\"";
                                    }
                                    jsonValueType = String.class.getName();

                                } else if (correspondingValue instanceof Boolean) {
                                    jsonValueType = Boolean.class.getName();
                                    valueStr = correspondingValue + "";

                                } else if (correspondingValue instanceof Number) {
                                    jsonValueType = Number.class.getName();
                                    valueStr = correspondingValue + "";
                                } else {
                                    valueStr = correspondingValue + "";
                                }
                            }

                            jsonValue = valueStr;
                            break;
                        }
                    }
                }

                if (StrUtil.isNotBlank(squareBracketsStart)) append(squareBracketsStart, lightAttributes, false);
                if (StrUtil.isNotBlank(nodeTypeStr)) append(nodeTypeStr, blueAttributes, false);
                if (StrUtil.isNotBlank(squareBracketsEnd)) append(squareBracketsEnd, lightAttributes, false);
                if (StrUtil.isNotBlank(sizeStrPre)) append(sizeStrPre, lightAttributes, false);
                if (StrUtil.isNotBlank(sizeStr)) append(sizeStr, purpleAttributes, false);
                if (StrUtil.isNotBlank(sizeStrPost)) append(sizeStrPost, lightAttributes, false);

                // 普通节点
                if (StrUtil.isNotBlank(jsonValue)) {
                    SimpleTextAttributes attributes;
                    if ("null".equals(jsonValueType) || Boolean.class.getName().equals(jsonValueType)) {
                        attributes = booleanWithNullColorAttributes;
                    } else if (String.class.getName().equals(jsonValueType)) {
                        attributes = stringColorAttributes;
                    } else if (Number.class.getName().equals(jsonValueType)) {
                        attributes = numberColorAttributes;
                    } else {
                        attributes = stringColorAttributes;
                    }

                    append(jsonValue, attributes, false);
                }

                setIcon(icon);
            }
        });
    }

    private void initPopupMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.addSeparator();
        group.add(new CopyKeyAction(tree));
        group.addSeparator();
        group.add(new CopyValueAction(tree));
        group.addSeparator();
        // group.add(new CopyKeyValueAction());
        group.addSeparator();
        // group.add(new ExpandMultiAction());
        group.addSeparator();
        // group.add(new CollapseMultiAction());
        group.addSeparator();
        group.add(new RemoveAction(tree));
        ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.POPUP, group);
        JPopupMenu popupMenu = actionPopupMenu.getComponent();

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // 获取选中的节点
                    TreePath[] paths = tree.getSelectionPaths();
                    if (ArrayUtil.isEmpty(paths)) {
                        int row = tree.getRowForLocation(e.getX(), e.getY());
                        if (row != -1) {
                            tree.setSelectionRow(row);
                            popupMenu.show(tree, e.getX(), e.getY());
                        }
                    } else {
                        popupMenu.show(tree, e.getX(), e.getY());
                    }
                }
            }
        });
    }




    // private class CopyKeyValueAction extends AnAction {
    //     public CopyKeyValueAction() {
    //         super("拷贝键值对..");
    //     }
    //
    //     @Override
    //     public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    //         TreePath[] paths = tree.getSelectionPaths();
    //         if (paths != null) {
    //             List<String> valueList = new ArrayList<>();
    //             for (TreePath path : paths) {
    //                 JsonCollectInfoMutableTreeNode node = (JsonCollectInfoMutableTreeNode) path.getLastPathComponent();
    //                 // 获取value值，多个的话用其他处理方式
    //                 Object correspondingValue = node.getCorrespondingValue();
    //                 JsonTreeNodeValueTypeEnum nodeValueType = node.getValueType();
    //
    //                 String userObject = node.getUserObject().toString();
    //                 // 只有JSONArrayEl是没有Value的
    //                 if (Objects.equals(JsonTreeNodeValueTypeEnum.JSONArrayEl, nodeValueType)) {
    //                     valueList.add(userObject);
    //                 } else if (Objects.equals(JsonTreeNodeValueTypeEnum.JSONObjectKey, nodeValueType)) {
    //                     valueList.add(userObject + ": " + (Objects.nonNull(correspondingValue) ? correspondingValue.toString() : "null"));
    //                 } else {
    //                     JSON json = (JSON) correspondingValue;
    //                     String item;
    //                     if (Objects.nonNull(json)) {
    //                         try {
    //                             item = CommonUtil.objectMapper.writeValueAsString(json);
    //                         } catch (JsonProcessingException ex) {
    //                             item = json.toJSONString(2);
    //                         }
    //                     } else {
    //                         item = "null";
    //                     }
    //
    //                     valueList.add(userObject + ": " + item);
    //                 }
    //             }
    //             ActionUtil.setClipboard(StrUtil.join(", \n", valueList));
    //         }
    //     }
    //
    //     @Override
    //     public @NotNull ActionUpdateThread getActionUpdateThread() {
    //         return ActionUpdateThread.BGT;
    //     }
    //
    //     @Override
    //     public void update(@NotNull AnActionEvent e) {
    //         TreePath[] paths = tree.getSelectionPaths();
    //         boolean visible = true;
    //         if (Objects.nonNull(paths) && paths.length == 1) {
    //             TreePath path = paths[0];
    //             JsonCollectInfoMutableTreeNode node = (JsonCollectInfoMutableTreeNode) path.getLastPathComponent();
    //             JsonTreeNodeValueTypeEnum nodeValueType = node.getValueType();
    //             if (Objects.equals(JsonTreeNodeValueTypeEnum.JSONArrayEl, nodeValueType)) {
    //                 visible = false;
    //             }
    //         }
    //
    //         e.getPresentation().setEnabledAndVisible(visible);
    //     }
    // }




    // private class ExpandMultiAction extends AnAction {
    //     public ExpandMultiAction() {
    //         super("完全展开..");
    //     }
    //
    //     @Override
    //     public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    //         TreePath[] paths = tree.getSelectionPaths();
    //         if (paths != null) {
    //             for (TreePath path : paths) {
    //                 // expandAll(path);
    //                 CommonUtil.expandAll(tree, path);
    //
    //             }
    //         }
    //     }
    //
    //     @Override
    //     public @NotNull ActionUpdateThread getActionUpdateThread() {
    //         return ActionUpdateThread.BGT;
    //     }
    //
    //     @Override
    //     public void update(@NotNull AnActionEvent e) {
    //         boolean enabled = false;
    //         TreePath[] paths = tree.getSelectionPaths();
    //         if (ArrayUtil.isNotEmpty(paths)) {
    //             for (TreePath path : paths) {
    //                 JsonCollectInfoMutableTreeNode node = (JsonCollectInfoMutableTreeNode) path.getLastPathComponent();
    //                 JsonTreeNodeValueTypeEnum nodeValueType = node.getValueType();
    //                 if (Objects.equals(nodeValueType, JsonTreeNodeValueTypeEnum.JSONObject)
    //                         || Objects.equals(nodeValueType, JsonTreeNodeValueTypeEnum.JSONArray)
    //                         || Objects.equals(nodeValueType, JsonTreeNodeValueTypeEnum.JSONObjectEl))
    //                     enabled = true;
    //             }
    //         }
    //
    //         e.getPresentation().setEnabledAndVisible(enabled);
    //     }
    // }

    // private class CollapseMultiAction extends AnAction {
    //     public CollapseMultiAction() {
    //         super("完全折叠..");
    //     }
    //
    //     @Override
    //     public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    //         TreePath[] paths = tree.getSelectionPaths();
    //         if (paths != null) {
    //             for (TreePath path : paths) {
    //                 // collapseAll(path);
    //                 CommonUtil.collapseAll(tree, path);
    //             }
    //         }
    //     }
    //
    //     @Override
    //     public @NotNull ActionUpdateThread getActionUpdateThread() {
    //         return ActionUpdateThread.BGT;
    //     }
    //
    //     @Override
    //     public void update(@NotNull AnActionEvent e) {
    //         boolean enabled = false;
    //         TreePath[] paths = tree.getSelectionPaths();
    //         if (ArrayUtil.isNotEmpty(paths)) {
    //             for (TreePath path : paths) {
    //                 JsonCollectInfoMutableTreeNode node = (JsonCollectInfoMutableTreeNode) path.getLastPathComponent();
    //                 JsonTreeNodeValueTypeEnum nodeValueType = node.getValueType();
    //                 if (Objects.equals(nodeValueType, JsonTreeNodeValueTypeEnum.JSONObject)
    //                         || Objects.equals(nodeValueType, JsonTreeNodeValueTypeEnum.JSONArray)
    //                         || Objects.equals(nodeValueType, JsonTreeNodeValueTypeEnum.JSONObjectEl))
    //                     enabled = true;
    //             }
    //         }
    //
    //         e.getPresentation().setEnabledAndVisible(enabled);
    //     }
    // }


}
