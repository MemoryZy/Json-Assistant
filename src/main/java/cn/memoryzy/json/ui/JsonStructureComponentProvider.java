package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.structure.*;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.model.EditorContext;
import cn.memoryzy.json.model.structure.StructureSetting;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.GeneralSettings;
import cn.memoryzy.json.service.persistent.state.TreeStructureState;
import cn.memoryzy.json.ui.listener.TreeRightClickPopupMenuMouseAdapter;
import cn.memoryzy.json.ui.tree.JsonFilterableTree;
import cn.memoryzy.json.ui.tree.JsonNode;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.*;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Memory
 * @since 2024/12/12
 */
public class JsonStructureComponentProvider {

    public static final SimpleTextAttributes LIGHT_ATTRIBUTES = SimpleTextAttributes.merge(SimpleTextAttributes.REGULAR_ATTRIBUTES, SimpleTextAttributes.GRAYED_ATTRIBUTES);
    public static final SimpleTextAttributes BLUE_ATTRIBUTES = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(63, 120, 230), new Color(137, 174, 246)));
    public static final SimpleTextAttributes PURPLE_ATTRIBUTES = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(248, 108, 101), new Color(244, 184, 181)));
    public static final SimpleTextAttributes STRING_COLOR_ATTRIBUTES = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(6, 125, 23), new Color(104, 169, 114)));
    public static final SimpleTextAttributes BOOLEAN_NULL_ATTRIBUTES = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(0, 51, 179), new Color(206, 141, 108)));
    public static final SimpleTextAttributes NUMBER_COLOR_ATTRIBUTES = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(25, 80, 234), new Color(41, 171, 183)));
    public static final SimpleTextAttributes PATH_COLOR_ATTRIBUTES = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(120, 80, 130), new Color(180, 140, 170)));


    private final Tree tree;
    private final JsonFilterableTree filterableTree;
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

        this.filterableTree = new JsonFilterableTree(null, new JsonNode("root").setJsonPath("$"), wrapper);
        this.tree = filterableTree.getTree();
        this.treeComponent = new JPanel(new BorderLayout());

        // 安装弹窗编辑器过滤
        filterableTree.installSimple();

        if (!setting.isLazyLoad()) {
            component = null == component ? treeComponent : component;
            init(wrapper, component, setting);
        }

        replenishTempPsiFile();
    }

    /**
     * 初始化组件（同时允许在后面再进行树的构建）
     *
     * @param wrapper   JSON 结构
     * @param component 注册快捷键的组件
     * @param setting   配置
     */
    private void init(JsonWrapper wrapper, @Nullable JComponent component, StructureSetting setting) {
        // 重构树
        filterableTree.setWrapper(wrapper).update();

        // 构建树
        tree.setDragEnabled(true);
        tree.setExpandableItemsEnabled(true);
        tree.setToggleClickCount(1);
        tree.setFont(UIUtils.JETBRAINS_MAPLE_MONO_FONT.deriveFont(12F));
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

        // 创建工具栏包装
        ToolbarDecorator decorator = createDecorator(component);
        // 展开指定层数节点
        UIUtils.expandSpecifiedLevelNode(tree, setting.getExpandLevel());
        this.treeComponent.add(decorator.createPanel(), BorderLayout.CENTER);

        // 初始化完毕
        initializationDone = true;
    }

    private ToolbarDecorator createDecorator(@Nullable JComponent component) {
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

        return decorator;
    }

    public void rebuildTree(JsonWrapper wrapper, int expandLevel, EditorContext editorContext) {
        this.editorContextReference.set(editorContext);
        replenishTempPsiFile();
        this.setting.setExpandLevel(expandLevel);

        // 之前因为懒加载没有执行样式加载，这里进行
        if (!initializationDone && setting.isLazyLoad()) {
            init(wrapper, treeComponent, setting);
        } else {
            // 重构树节点
            filterableTree.setWrapper(wrapper).update();
            // 默认展开前3级节点
            UIUtils.expandSpecifiedLevelNode(tree, expandLevel);
        }
    }


    // TODO 还需要设置，在点击树化时，把焦点切换到树上

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
        group.add(new ModifyNodeValueAction(tree, editorContextReference, filterableTree));
        group.addSeparator();
        // group.add(new NavigateToSourceAction(tree, editorContextReference));
        // group.addSeparator();
        group.add(new ShowAsTableAction(tree));
        group.addSeparator();
        group.add(new ExpandMultiAction(tree));
        group.addSeparator();
        group.add(new CollapseMultiAction(tree));
        group.addSeparator();
        group.add(new RemoveTreeNodeAction(tree, filterableTree));
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

    public void requestFocusOnStructureComponent() {
        IdeFocusManager.findInstance().requestFocus(tree, true);
    }

    private class StyleTreeCellRenderer extends ColoredTreeCellRenderer {

        private final Font chineseFont;
        // private final Font baseFont;

        public StyleTreeCellRenderer() {
            this.chineseFont = UIUtils.JETBRAINS_MAPLE_MONO_FONT;
            // this.baseFont = UIUtils.jetBrainsMonoFont(JBUIScale.scaleFontSize(13));
        }

        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            JsonNode jsonNode = (JsonNode) node.getUserObject();
            JsonTreeNodeType nodeType = jsonNode.getNodeType();

            if (nodeType == null) return;

            // 1. 提取节点类型渲染信息
            NodeRenderData renderData = resolveNodeRenderData(nodeType, jsonNode);
            setIcon(renderData.icon);

            // 2. 渲染主文本（键名）
            if (nodeType != JsonTreeNodeType.JSONArrayElement) {
                String text = String.valueOf(jsonNode.getKey());
                String prefix = (nodeType == JsonTreeNodeType.JSONObjectProperty) ? text + ": " : text;
                append(prefix, SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
            }

            // 3. 渲染类型标识符（如 [object]）
            appendIfNonBlank(renderData.typePrefix, LIGHT_ATTRIBUTES);
            appendIfNonBlank(renderData.typeLabel, BLUE_ATTRIBUTES);
            appendIfNonBlank(renderData.typeSuffix, LIGHT_ATTRIBUTES);
            appendIfNonBlank(renderData.sizePrefix, LIGHT_ATTRIBUTES);
            appendIfNonBlank(renderData.sizeDescription, PURPLE_ATTRIBUTES);
            appendIfNonBlank(renderData.sizeSuffix, LIGHT_ATTRIBUTES);

            // 4. 渲染值（若存在）
            if (renderData.formattedValue != null) {
                SimpleTextAttributes valueAttrs;
                switch (renderData.valueType) {
                    case "null":
                    case "java.lang.Boolean": {
                        valueAttrs = BOOLEAN_NULL_ATTRIBUTES;
                        break;
                    }
                    case "java.lang.Number": {
                        valueAttrs = NUMBER_COLOR_ATTRIBUTES;
                        break;
                    }
                    default: {
                        valueAttrs = STRING_COLOR_ATTRIBUTES;
                        break;
                    }
                }

                append(renderData.formattedValue, valueAttrs, true);
            }

            // 5. 渲染注释
            appendComment(jsonNode.getComment());

            // 6. 渲染路径提示
            renderPathHint(jsonNode, node);

            // 7.根据匹配切换字体
            // if (jsonNode.isMatched()) {
            //     setFont(chineseFont);
            // } else {
            //     setFont(baseFont);
            // }
        }


        // --------------------------- 重构的辅助方法 --------------------------- //

        private NodeRenderData resolveNodeRenderData(JsonTreeNodeType nodeType, JsonNode node) {
            NodeRenderData data = new NodeRenderData();
            switch (nodeType) {
                case JSONObject: {
                    configureObjectNode(data, node);
                    break;
                }
                case JSONArray: {
                    configureArrayNode(data, node);
                    break;
                }
                case JSONObjectElement: {
                    configureObjectElement(data, node);
                    break;
                }
                case JSONArrayElementArray: {
                    configureArrayElementArray(data, node);
                    break;
                }
                case JSONArrayElement:
                case JSONObjectProperty: {
                    configureValueNode(data, node);
                    break;
                }
            }

            return data;
        }


        private void configureObjectNode(NodeRenderData data, JsonNode node) {
            data.icon = JsonAssistantIcons.Structure.JSON_OBJECT;
            data.typePrefix = " [";
            data.typeLabel = "object";
            data.typeSuffix = "]";
            data.sizeDescription = node.getSize() + " " + getSizeText("obj", node.getSize());
            wrapSizeString(data);
        }

        private void configureArrayNode(NodeRenderData data, JsonNode node) {
            data.icon = JsonAssistantIcons.Structure.JSON_ARRAY;
            data.typePrefix = " [";
            data.typeLabel = "array";
            data.typeSuffix = "]";
            data.sizeDescription = node.getSize() + " " + getSizeText("array", node.getSize());
            wrapSizeString(data);
        }

        private void configureObjectElement(NodeRenderData data, JsonNode node) {
            data.icon = JsonAssistantIcons.Structure.JSON_OBJECT_ITEM;
            data.typePrefix = " [";
            data.typeLabel = "array_object";
            data.typeSuffix = "]";
            data.sizeDescription = node.getSize() + " " + getSizeText("obj", node.getSize());
            wrapSizeString(data);
        }

        private void configureArrayElementArray(NodeRenderData data, JsonNode node) {
            data.icon = JsonAssistantIcons.Structure.JSON_ARRAY;
            data.typePrefix = " [";
            data.typeLabel = "array_array";
            data.typeSuffix = "]";
            data.sizeDescription = node.getSize() + " " + getSizeText("array", node.getSize());
            wrapSizeString(data);
        }

        private void configureValueNode(NodeRenderData data, JsonNode node) {
            Object value = node.getValue();
            data.formattedValue = formatNodeValue(value);
            data.valueType = value == null ? "null" : value.getClass().getName();
            data.icon = (node.getNodeType() == JsonTreeNodeType.JSONArrayElement)
                    ? JsonAssistantIcons.Structure.JSON_ITEM
                    : JsonAssistantIcons.Structure.JSON_KEY;
        }

        private void appendComment(String comment) {
            if (StrUtil.isNotBlank(comment)) {
                append("  " + comment, SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES, false);
            }
        }

        private void renderPathHint(JsonNode node, DefaultMutableTreeNode treeNode) {
            if (!structureState.isDisplayNodePath() || !treeNode.equals(hoverNode)) {
                setToolTipText(null);
                return;
            }

            String path = node.getJsonPath();
            setToolTipText(path);
            if (treeNode.getPath().length > 2) append("  " + path, PATH_COLOR_ATTRIBUTES, false);
        }

        private void appendIfNonBlank(String text, SimpleTextAttributes attrs) {
            if (StrUtil.isNotBlank(text)) append(text, attrs, false);
        }

        private String getSizeText(String type, int size) {
            String key = size == 1 ?
                    "dialog.structure.size." + type + ".singular.text" :
                    "dialog.structure.size." + type + ".plural.text";
            return JsonAssistantBundle.messageOnSystem(key);
        }

        private void wrapSizeString(NodeRenderData data) {
            if (data.sizeDescription != null) {
                data.sizePrefix = " (";
                data.sizeSuffix = ")";
            }
        }

    }

    public static String formatNodeValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String) {
            String str = (String) value;
            return str.isEmpty() ? "\"\"" : "\"" + str + "\"";
        }
        return String.valueOf(value);
    }


    /**
     * 封装树节点渲染所需数据
     */
    private static class NodeRenderData {

        /**
         * 方括号开始部分（如" ["），用于表示结构化节点的开始
         * 通常用于JSON对象和数组类型的节点
         */
        String typePrefix = "";

        /**
         * 节点类型描述字符串（如"object"、"array"）
         * 显示在方括号内，标识节点的JSON类型
         */
        String typeLabel = "";

        /**
         * 方括号结束部分（如"]"），与squareBracketsStart配对使用
         */
        String typeSuffix = "";

        /**
         * 尺寸信息的前缀字符串（如" ("）
         * 用于包裹节点包含的元素数量信息
         */
        String sizePrefix = "";

        /**
         * 尺寸信息字符串（如"3 objects"）
         * 包含节点包含的元素数量和类型描述，使用国际化消息
         */
        String sizeDescription = "";

        /**
         * 尺寸信息的后缀字符串（如")"）
         * 与sizeStrPre配对使用，形成完整的尺寸信息包裹
         */
        String sizeSuffix = "";

        /**
         * 节点的值字符串表示（如"\"text\""、"123"、"null"）
         * 对于基本类型节点（字符串、数字、布尔值、null）存储格式化后的值
         * 结构化节点（对象、数组）此字段为null
         */
        String formattedValue = null;

        /**
         * 节点值的类型标识（如"java.lang.String"、"java.lang.Number"、"null"）
         * 用于确定值的渲染颜色和样式
         */
        String valueType = "";

        /**
         * 节点对应的图标资源
         * 根据节点类型使用不同的图标（如JSON对象图标、数组图标、键值对图标等）
         * 默认值为{@link JsonAssistantIcons.Structure#JSON_KEY}
         */
        Icon icon = JsonAssistantIcons.Structure.JSON_KEY;

    }

}
