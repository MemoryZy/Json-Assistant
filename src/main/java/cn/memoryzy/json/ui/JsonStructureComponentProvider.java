package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.structure.*;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.model.EditorContext;
import cn.memoryzy.json.model.structure.StructureSetting;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.service.persistent.GeneralSettings;
import cn.memoryzy.json.service.persistent.state.TreeStructureState;
import cn.memoryzy.json.ui.listener.TreeRightClickPopupMenuMouseAdapter;
import cn.memoryzy.json.ui.tree.JsonTreeNode;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.*;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Memory
 * @since 2024/12/12
 */
public class JsonStructureComponentProvider {

    private final Tree tree;
    private final JPanel treeComponent;
    private Object hoverNode;
    private final TreeStructureState structureState;
    private final StructureSetting setting;

    /**
     * 是否已初始化完毕
     */
    private boolean initializationDone;

    /**
     * 上下文或上下文内的内容
     * <br/>
     * <br/>
     * <b>注意：</b>
     * <p>因为在初始化此类时，ModifyNodeValueAction 就已经被构建了，而它取到的EditorContext是一开始创建此类时的默认实例，不包含任何信息。</p>
     * <p>后续使用此ModifyNodeValueAction类操作时，其 EditorContext 永远是默认的那个实例，所以需要将其改为函数式获取，或者用一个引用包裹。</p>
     */
    private final AtomicReference<EditorContext> editorContextReference = new AtomicReference<>();

    // TODO 尝试按需解析，初始只解析到第2层级，展开节点时动态加载子树（类似IDE的大文件处理）

    /**
     * 构造器
     *
     * @param wrapper   JSON 结构
     * @param component 注册快捷键的组件
     * @param setting   配置
     */
    public JsonStructureComponentProvider(JsonWrapper wrapper, @Nullable JComponent component, StructureSetting setting) {
        this.structureState = GeneralSettings.getInstance().getState().getTreeStructureState();
        this.setting = setting;
        this.editorContextReference.set(setting.getEditorContext());
        this.tree = new Tree(new DefaultTreeModel(new JsonTreeNode("root").setJsonPath("$")));
        this.treeComponent = new JPanel(new BorderLayout());

        if (!setting.isLazyLoad()) {
            component = null == component ? treeComponent : component;
            init(wrapper, component, setting);
        }

        replenishTempPsiFile();
    }

    /**
     * 初始化组件
     *
     * @param wrapper   JSON 结构
     * @param component 注册快捷键的组件
     * @param setting   配置
     */
    private void init(JsonWrapper wrapper, @Nullable JComponent component, StructureSetting setting) {
        JsonTreeNode rootNode = (JsonTreeNode) tree.getModel().getRoot();
        // 允许在后面再进行树的构建
        if (wrapper != null) {
            convertToTreeNode(wrapper, rootNode, "$");
        }

        // 构建树
        tree.setDragEnabled(true);
        tree.setExpandableItemsEnabled(true);
        tree.setFont(UIUtils.jetBrainsMonoFont(12));
        tree.setCellRenderer(new StyleTreeCellRenderer());
        tree.addMouseListener(new TreeRightClickPopupMenuMouseAdapter(tree, buildRightMousePopupMenu()));
        tree.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                hoverNode = (path != null) ? path.getLastPathComponent() : null;
                tree.repaint();
            }
        });

        // 触发快速检索
        new TreeSpeedSearch(tree);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(tree);
        if (setting.isNeedToolbar()) {
            if (setting.isNeedRefresh()) {
                decorator.addExtraAction(AnActionButton.fromAction(new RefreshStructureAction(this, setting.getFile())));
            }

            decorator.addExtraAction(new ExpandAllAction(tree, component, true))
                    .addExtraAction(new CollapseAllAction(tree, component, true))
                    .addExtraAction(AnActionButton.fromAction(new DisplayNodePathAction(structureState)));
        } else {
            decorator.setPanelBorder(JBUI.Borders.empty());
        }

        if (!setting.isNeedBorder()) {
            // 去除边框
            decorator.setPanelBorder(JBUI.Borders.empty(0, 1))
                    .setScrollPaneBorder(JBUI.Borders.empty(0, 1));
        }

        UIUtils.expandSpecifiedLevelNode(tree, setting.getExpandLevel());

        this.treeComponent.add(decorator.createPanel(), BorderLayout.CENTER);

        // 设置初始化完毕
        initializationDone = true;
    }

    public void rebuildTree(JsonWrapper wrapper, int expandLevel, EditorContext editorContext) {
        this.editorContextReference.set(editorContext);
        replenishTempPsiFile();
        this.setting.setExpandLevel(expandLevel);

        // 之前因为懒加载没有执行样式加载，这里进行
        if (!initializationDone && setting.isLazyLoad()) {
            init(wrapper, treeComponent, setting);
        } else {
            JsonTreeNode rootNode = new JsonTreeNode("root").setJsonPath("$");
            if (wrapper != null) {
                convertToTreeNode(wrapper, rootNode, "$");
            }

            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            model.setRoot(rootNode);

            UIUtils.repaintComponent(tree);

            // 默认展开前3级节点
            UIUtils.expandSpecifiedLevelNode(tree, expandLevel);
        }
    }

    private void convertToTreeNode(JsonWrapper jsonWrapper, JsonTreeNode parentNode, String parentPath) {
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
                JsonTreeNode childNode = new JsonTreeNode(key)
                        .setComment(comment)
                        .setJsonPath(currentPath);

                if (value instanceof ObjectWrapper) {
                    ObjectWrapper nestedJsonObject = (ObjectWrapper) value;
                    childNode.setValue(value)
                            .setNodeType(JsonTreeNodeType.JSONObject)
                            .setSize(nestedJsonObject.size());
                    convertToTreeNode(nestedJsonObject, childNode, currentPath);

                } else if (value instanceof ArrayWrapper) {
                    ArrayWrapper jsonArray = (ArrayWrapper) value;
                    childNode.setValue(value)
                            .setNodeType(JsonTreeNodeType.JSONArray)
                            .setSize(jsonArray.size());
                    handleJsonArray(childNode, jsonArray, currentPath);

                } else {
                    // 若不是对象或数组，则不添加子集，直接同层级
                    childNode.setValue(value)
                            .setNodeType(JsonTreeNodeType.JSONObjectProperty)
                            .setUserObject(key);
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

            handleJsonArray(parentNode, jsonArray, parentPath);
        }
    }

    private void handleJsonArray(JsonTreeNode parentNode, ArrayWrapper jsonArray, String parentPath) {
        for (int i = 0; i < jsonArray.size(); i++) {
            Object element = jsonArray.get(i);

            // 构建当前节点路径
            String currentPath = buildArrayElementPath(parentPath, i);

            if (element instanceof ObjectWrapper) {
                ObjectWrapper jsonObjectElement = (ObjectWrapper) element;
                JsonTreeNode childNode = new JsonTreeNode("item" + i)
                        .setValue(element)
                        .setNodeType(JsonTreeNodeType.JSONObjectElement)
                        .setSize(jsonObjectElement.size())
                        .setJsonPath(currentPath);

                convertToTreeNode(jsonObjectElement, childNode, currentPath);
                parentNode.add(childNode);

            } else if (element instanceof ArrayWrapper) {
                ArrayWrapper jsonArrayElement = (ArrayWrapper) element;
                JsonTreeNode childNodeElement = new JsonTreeNode("item" + i)
                        .setValue(element)
                        .setNodeType(JsonTreeNodeType.JSONArrayElementArray)
                        .setSize(jsonArrayElement.size())
                        .setJsonPath(currentPath);

                convertToTreeNode(jsonArrayElement, childNodeElement, currentPath);
                parentNode.add(childNodeElement);
            } else {
                Object obj = element;
                if (element instanceof String) {
                    String str = (String) element;
                    obj = "\"" + str + "\"";
                }

                JsonTreeNode childNode = new JsonTreeNode(obj)
                        .setValue(element)
                        .setNodeType(JsonTreeNodeType.JSONArrayElement)
                        .setJsonPath(currentPath);

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

    private JPopupMenu buildRightMousePopupMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.addSeparator();
        group.add(new CopyKeyAction(tree));
        group.addSeparator();
        group.add(new CopyValueAction(tree));
        group.addSeparator();
        group.add(new CopyKeyValueAction(tree));
        group.addSeparator();
        group.add(new CopyNodePathAction(tree));
        group.addSeparator();
        group.add(new CopyNodeCommentAction(tree));
        group.addSeparator();
        group.add(new ModifyNodeValueAction(tree, editorContextReference));
        group.addSeparator();
        // group.add(new NavigateToSourceAction(tree, editorContextReference));
        // group.addSeparator();
        group.add(new ShowAsTableAction(tree));
        group.addSeparator();
        group.add(new ExpandMultiAction(tree));
        group.addSeparator();
        group.add(new CollapseMultiAction(tree));
        group.addSeparator();
        group.add(new RemoveTreeNodeAction(tree));
        ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.POPUP, group);
        return actionPopupMenu.getComponent();
    }

    private void replenishTempPsiFile() {
        EditorContext editorContext = editorContextReference.get();
        PsiFile psiFile = editorContext.getPsiFile();
        // 如果源PSI文件为null，说明我们连一个可用的PSI文件都没有，此时创建临时文件也没有意义
        if (psiFile == null) return;

        String content = editorContext.getContentText();
        if (!JsonUtil.isJson(content) && !Json5Util.isJson5(content)) return;

        editorContext.setOriginalContent(content);

        // 如果不是JSON文件，则创建临时PSI文件
        if (PlatformUtil.isJsonFile(psiFile)) return;
        FileType fileType = FileTypeHolder.JSON5;
        PsiFile tempFile = PsiFileFactory.getInstance(psiFile.getProject())
                .createFileFromText("temp." + fileType.getDefaultExtension(), fileType, content);

        editorContext.setTempPsiFile(tempFile);
    }

    public Tree getTree() {
        return tree;
    }

    public JPanel getTreeComponent() {
        return treeComponent;
    }

    private class StyleTreeCellRenderer extends ColoredTreeCellRenderer {
        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JsonTreeNode jsonTreeNode = (JsonTreeNode) value;
            JsonTreeNodeType nodeType = jsonTreeNode.getNodeType();

            String text = String.valueOf(jsonTreeNode.getUserObject());
            SimpleTextAttributes simpleTextAttributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;

            SimpleTextAttributes lightAttributes = SimpleTextAttributes.merge(simpleTextAttributes, SimpleTextAttributes.GRAYED_ATTRIBUTES);
            SimpleTextAttributes blueAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(63, 120, 230), new Color(137, 174, 246)));
            SimpleTextAttributes purpleAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(248, 108, 101), new Color(244, 184, 181)));

            SimpleTextAttributes stringColorAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(6, 125, 23), new Color(104, 169, 114)));
            SimpleTextAttributes booleanWithNullColorAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(0, 51, 179), new Color(206, 141, 108)));
            SimpleTextAttributes numberColorAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(25, 80, 234), new Color(41, 171, 183)));

            SimpleTextAttributes pathColorAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(120, 80, 130), new Color(180, 140, 170)));

            Icon icon = JsonAssistantIcons.Structure.JSON_KEY;

            Integer size = jsonTreeNode.getSize();
            Object nodeValue = jsonTreeNode.getValue();

            String squareBracketsStart = "";
            String nodeTypeStr = "";
            String squareBracketsEnd = "";

            String sizeStrPre = "";
            String sizeStr = "";
            String sizeStrPost = "";

            // json的value
            String jsonValue = "";
            String jsonValueType = "";

            if (Objects.nonNull(nodeType)) {
                switch (nodeType) {
                    case JSONObject: {
                        squareBracketsStart = " [";
                        nodeTypeStr = "object";
                        squareBracketsEnd = "]";
                        sizeStrPre = " (";
                        sizeStr = size + " " + JsonAssistantBundle.messageOnSystem(size == 1 ? "dialog.structure.size.obj.singular.text" : "dialog.structure.size.obj.plural.text");
                        sizeStrPost = ")";

                        icon = JsonAssistantIcons.Structure.JSON_OBJECT;
                        break;
                    }

                    case JSONArray: {
                        squareBracketsStart = " [";
                        nodeTypeStr = "array";
                        squareBracketsEnd = "]";
                        sizeStrPre = " (";
                        sizeStr = size + " " + JsonAssistantBundle.messageOnSystem(size == 1 ? "dialog.structure.size.array.singular.text" : "dialog.structure.size.array.plural.text");
                        sizeStrPost = ")";

                        icon = JsonAssistantIcons.Structure.JSON_ARRAY;
                        break;
                    }

                    case JSONObjectElement: {
                        squareBracketsStart = " [";
                        nodeTypeStr = "array_object";
                        squareBracketsEnd = "]";
                        sizeStrPre = " (";
                        sizeStr = size + " " + JsonAssistantBundle.messageOnSystem(size == 1 ? "dialog.structure.size.obj.singular.text" : "dialog.structure.size.obj.plural.text");
                        sizeStrPost = ")";

                        icon = JsonAssistantIcons.Structure.JSON_OBJECT_ITEM;
                        break;
                    }

                    case JSONArrayElementArray: {
                        squareBracketsStart = " [";
                        nodeTypeStr = "array_array";
                        squareBracketsEnd = "]";
                        sizeStrPre = " (";
                        sizeStr = size + " " + JsonAssistantBundle.messageOnSystem(size == 1 ? "dialog.structure.size.array.singular.text" : "dialog.structure.size.array.plural.text");
                        sizeStrPost = ")";

                        icon = JsonAssistantIcons.Structure.JSON_ARRAY;
                        break;
                    }

                    case JSONArrayElement: {
                        icon = JsonAssistantIcons.Structure.JSON_ITEM;
                        String valueStr;
                        if (Objects.isNull(nodeValue)) {
                            valueStr = "null";
                            jsonValueType = "null";
                        } else {
                            if (nodeValue instanceof String) {
                                String str = (String) nodeValue;

                                if (str.isEmpty()) {
                                    valueStr = "\"\"";
                                } else {
                                    valueStr = "\"" + str + "\"";
                                }
                                jsonValueType = String.class.getName();

                            } else if (nodeValue instanceof Boolean) {
                                jsonValueType = Boolean.class.getName();
                                valueStr = nodeValue + "";

                            } else if (nodeValue instanceof Number) {
                                jsonValueType = Number.class.getName();
                                valueStr = nodeValue + "";
                            } else {
                                valueStr = nodeValue + "";
                            }
                        }

                        jsonValue = valueStr;
                        break;
                    }

                    case JSONObjectProperty: {
                        String valueStr;
                        if (Objects.isNull(nodeValue)) {
                            valueStr = "null";
                            jsonValueType = "null";
                        } else {
                            if (nodeValue instanceof String) {
                                String str = (String) nodeValue;

                                if (str.isEmpty()) {
                                    valueStr = "\"\"";
                                } else {
                                    valueStr = "\"" + str + "\"";
                                }
                                jsonValueType = String.class.getName();

                            } else if (nodeValue instanceof Boolean) {
                                jsonValueType = Boolean.class.getName();
                                valueStr = nodeValue + "";

                            } else if (nodeValue instanceof Number) {
                                jsonValueType = Number.class.getName();
                                valueStr = nodeValue + "";
                            } else {
                                valueStr = nodeValue + "";
                            }
                        }

                        jsonValue = valueStr;
                        break;
                    }
                }
            }

            if (!Objects.equals(JsonTreeNodeType.JSONArrayElement, nodeType)) {
                append(Objects.equals(JsonTreeNodeType.JSONObjectProperty, nodeType) ? text + ": " : text, simpleTextAttributes);
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

                append(jsonValue, attributes, Objects.equals(JsonTreeNodeType.JSONArrayElement, nodeType));
            }

            // 添加注释（如有）
            String comment = jsonTreeNode.getComment();
            if (StrUtil.isNotBlank(comment)) {
                append("  " + comment, SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES, false);
            }

            if (structureState.isDisplayNodePath() && jsonTreeNode.equals(hoverNode)) {
                TreeNode[] pathElements = jsonTreeNode.getPath();
                // 不显示根节点与第二层的节点路径
                if (pathElements.length > 2) {
                    // 悬停时显示完整路径
                    String pathResult = jsonTreeNode.getJsonPath();
                    // 同时显示工具提示
                    setToolTipText(pathResult);

                    // 路径换个颜色
                    append("  " + pathResult, pathColorAttributes, false);
                }
            } else {
                setToolTipText(null);
            }

            setIcon(icon);
        }
    }

}
