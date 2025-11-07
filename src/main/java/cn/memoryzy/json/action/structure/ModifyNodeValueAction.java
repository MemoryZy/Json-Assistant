package cn.memoryzy.json.action.structure;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.model.EditorContext;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.service.persistent.GeneralSettings;
import cn.memoryzy.json.service.persistent.state.TreeStructureState;
import cn.memoryzy.json.ui.decorator.EditorErrorPopupManager;
import cn.memoryzy.json.ui.editor.ExpandableEditorTextField;
import cn.memoryzy.json.ui.tree.JsonFilterableTree;
import cn.memoryzy.json.ui.tree.JsonNode;
import cn.memoryzy.json.util.*;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.json.json5.Json5Language;
import com.intellij.json.psi.JsonElement;
import com.intellij.json.psi.JsonElementGenerator;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Memory
 * @since 2025/7/16
 */
public class ModifyNodeValueAction extends DumbAwareAction implements UpdateInBackground {

    private static final Logger log = LoggerFactory.getLogger(ModifyNodeValueAction.class);
    private final Tree tree;
    private final JsonFilterableTree filterableTree;
    private final AtomicReference<EditorContext> editorContextReference;
    private final TreeStructureState structureState;

    private Balloon currentBalloon;
    private ExpandableEditorTextField expandableTextField;
    private EditorErrorPopupManager decorator;
    private JBCheckBox sourceFileCheckBox;
    private JButton saveButton;

    public ModifyNodeValueAction(Tree tree, AtomicReference<EditorContext> editorContextReference, JsonFilterableTree filterableTree) {
        super(JsonAssistantBundle.messageOnSystem("action.modifyNodeValue.text"), JsonAssistantBundle.messageOnSystem("action.modifyNodeValue.description"), null);
        this.tree = tree;
        this.filterableTree = filterableTree;
        this.editorContextReference = editorContextReference;
        this.structureState = GeneralSettings.getInstance().getState().getTreeStructureState();
    }

    // TODO 再加上一个action，作用是跳转到对应的节点文本位置

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        // 获取选中的树节点
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        JsonNode node = (JsonNode) selectedNode.getUserObject();

        // 构建面板
        JComponent component = createComponent(project, node);
        // 获取节点在屏幕上的位置
        RelativePoint point = getNodeScreenPosition(selectedNode);
        // 构建气球
        this.currentBalloon = JBPopupFactory.getInstance().createDialogBalloonBuilder(component, null)
                .setShowCallout(true)
                .setCloseButtonEnabled(false)
                .setAnimationCycle(3)
                .setHideOnKeyOutside(true)
                .setHideOnClickOutside(true)
                .setRequestFocus(true)
                .setBlockClicksThroughBalloon(true)
                .setShadow(true)
                .createBalloon();

        this.currentBalloon.show(point, Balloon.Position.below);
        JRootPane rootPane = component.getRootPane();
        rootPane.setDefaultButton(saveButton);
        this.expandableTextField.requestFocus();
        this.decorator = new EditorErrorPopupManager(rootPane, expandableTextField);
    }

    private JComponent createComponent(Project project, JsonNode node) {
        this.expandableTextField = new ExpandableEditorTextField(project, Json5Language.INSTANCE);
        this.expandableTextField.setText(String.valueOf(node.getValue()));
        this.expandableTextField.selectAll();

        // 编辑器
        this.expandableTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeSaveAction(project, node);
                }
            }
        });
        this.expandableTextField.setShowPlaceholderWhenFocused(true);
        this.expandableTextField.setPlaceholder(JsonAssistantBundle.messageOnSystem("popup.modifyNodeValue.placeholder"));

        // 创建操作按钮
        this.saveButton = new JButton(JsonAssistantBundle.messageOnSystem("popup.modifyNodeValue.save.button.text"));
        this.saveButton.addActionListener(event -> executeSaveAction(project, node));

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);

        // 保存配置状态
        this.sourceFileCheckBox = new JBCheckBox(JsonAssistantBundle.messageOnSystem("popup.modifyNodeValue.applyToSourceFile.text"), structureState.isShouldApplyToSource());
        sourceFileCheckBox.addActionListener(
                event -> structureState.setShouldApplyToSource(sourceFileCheckBox.isSelected()));

        // 底部面板（选项+按钮）
        BorderLayoutPanel bottomPanel = new BorderLayoutPanel()
                .addToLeft(sourceFileCheckBox)
                .addToRight(buttonPanel);

        BorderLayoutPanel panel = new BorderLayoutPanel()
                .addToCenter(expandableTextField)
                .addToBottom(bottomPanel);
        panel.setPreferredSize(new Dimension(220, 79));
        panel.setBorder(JBUI.Borders.empty(10, 3, 0, 3));
        return panel;
    }

    private void executeSaveAction(Project project, JsonNode node) {
        EditorContext editorContext = editorContextReference.get();
        // 1.输入内容判断及处理（如果不为数值、布尔、null，则都为字符串。如果用户手动加了字符串，那都以字符串为准）
        String inputText = StrUtil.trim(expandableTextField.getText());
        if (StrUtil.isBlank(inputText)) {
            decorator.setError(JsonAssistantBundle.messageOnSystem("error.invalid.value"));
            return;
        }

        if (Objects.equals(String.valueOf(node.getValue()), inputText)) {
            decorator.setError(JsonAssistantBundle.messageOnSystem("error.content.repetition.content"));
            return;
        }

        // 解析输入文本，得到对应的值
        Object inputValue = parseInputText(inputText);

        // 2.判断是否应用到源文件
        if (structureState.isShouldApplyToSource()) {
            JsonElementGenerator generator = new JsonElementGenerator(project);

            // 获取 JSON 整体对应的 PSI 文件
            PsiFile effectivePsiFile = editorContext.getEffectivePsiFile();
            // 编辑器
            Editor editor = editorContext.getEditor();

            if (null == editor || editor.isDisposed()) {
                decorator.setError(JsonAssistantBundle.messageOnSystem("error.editor.disposed.content"));
                return;
            }

            // 定位到对应的 PSI 元素  TODO 这里可能只能暂时修改节点值，之后扩展为：可修改键
            Pair<JsonElement, JsonValue> elementPair = JsonPsiLocator.locateByPath(effectivePsiFile, node.getJsonPath());
            if (elementPair == null) {
                decorator.setError(JsonAssistantBundle.messageOnSystem("error.content.changed.content"));
                return;
            }

            String valueStr;
            if (null == inputValue) {
                valueStr = "null";
            } else if (inputValue instanceof String) {
                valueStr = "\"" + inputValue + "\"";
            } else if (inputValue instanceof JsonWrapper) {
                valueStr = ((JsonWrapper) inputValue).toJsonString();
            } else {
                valueStr = inputValue.toString();
            }

            // 替换
            JsonValue value = generator.createValue(valueStr);
            WriteCommandAction.runWriteCommandAction(project, () -> {
                // 修改文档值
                JsonValue jsonValue = elementPair.getSecond();
                jsonValue.replace(value);

                // 在此需要判断是否是临时 JsonFile，如果是的话，还需要把值赋予编辑器
                if (editorContext.isUsingTempFile()) {
                    // 前后文对比
                    String nowContent = editorContext.getContentText();
                    String originalContent = editorContext.getOriginalContent();

                    if (!Objects.equals(nowContent, originalContent)) {
                        decorator.setError(JsonAssistantBundle.messageOnSystem("error.content.changed.content"));
                        return;
                    }

                    // 全文替换
                    PlatformUtil.safeSetDocumentText(project, editor.getDocument(), effectivePsiFile.getText());
                }

                // 高亮显示被修改的位置
                TextAttributesKey textAttributesKey = EditorColors.LIVE_TEMPLATE_ATTRIBUTES;
                // 获取元素在文档中的位置
                JsonElement first = elementPair.getFirst();
                TextRange textRange = first.getTextRange();
                int startOffset = textRange.getStartOffset();
                int endOffset = textRange.getEndOffset();

                HighlightManager.getInstance(project)
                        .addRangeHighlight(
                                Objects.requireNonNull(editorContext.getEditor()),
                                startOffset,
                                endOffset,
                                textAttributesKey,
                                false,
                                null);

                // 更新树
                repaintTree(inputValue, node);
            });
        } else {
            // 更新树
            repaintTree(inputValue, node);
        }

        // 关闭弹窗
        if (null != currentBalloon) {
            currentBalloon.hide();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean isEnabled = false;
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        if (null != selectedNode) {
            JsonNode node = (JsonNode) selectedNode.getUserObject();
            EditorContext editorContext = editorContextReference.get();
            PsiFile effectivePsiFile = editorContext.getEffectivePsiFile();
            // TODO 目前只支持修改叶子节点

            // 叶子节点
            isEnabled = JsonTreeNodeType.isLeafNode(node.getNodeType())
                    // 存在JSON环境
                    && PlatformUtil.hasJsonEnvironment(e.getProject())
                    // 存在有效源文件
                    && (effectivePsiFile instanceof JsonFile && effectivePsiFile.isValid());
        }

        e.getPresentation().setEnabledAndVisible(isEnabled);
    }


    @Nullable
    private DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) UIUtils.getSelectedNode(tree);
    }

    private RelativePoint getNodeScreenPosition(DefaultMutableTreeNode node) {
        // 获取节点在树中的路径
        TreePath path = new TreePath(node.getPath());

        // 获取节点的渲染矩形
        Rectangle bounds = tree.getPathBounds(path);
        if (bounds == null) return null;

        // 转换为屏幕坐标
        Point screenPoint = new Point(
                bounds.x + bounds.width / 2,
                bounds.y + bounds.height / 2
        );

        return new RelativePoint(tree, screenPoint);
    }

    private Object parseInputText(String inputText) {
        // 规则1：用户手动添加了引号（强制作为字符串处理）
        if (inputText.startsWith("\"") && inputText.endsWith("\"")) {
            // 去除引号
            return inputText.substring(1, inputText.length() - 1);
        }

        // 规则2：尝试解析为数值或布尔值
        try {
            // 解析为整数
            if (inputText.matches("-?\\d+")) {
                return Long.parseLong(inputText);
            }
            // 解析为浮点数
            if (inputText.matches("-?\\d+(\\.\\d+)?")) {
                return new BigDecimal(inputText);
            }
            // 解析为布尔值
            if (inputText.equalsIgnoreCase("true") || inputText.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(inputText);
            }
            // 解析为 null
            if (inputText.equalsIgnoreCase("null")) {
                return null;
            }
            // 解析为 JSON
            if (JsonUtil.isJson(inputText)) {
                return JsonUtil.parse(inputText);
            }
            // 解析为 JSON5
            if (Json5Util.isJson5(inputText)) {
                // 先不管注释
                return Json5Util.parse(inputText);
            }

        } catch (Exception ignored) {
            // 解析失败时继续尝试其他规则
        }

        // 规则3：默认作为字符串处理
        return inputText;
    }

    private void repaintTree(Object inputValue, JsonNode node) {
        // 存储展开节点
        List<TreePath> expandedPaths = TreeUtil.collectExpandedPaths(tree);
        expandedPaths.stream()
                .map(path -> (DefaultMutableTreeNode) path.getLastPathComponent())
                .map(treeNode -> (JsonNode) treeNode.getUserObject())
                .forEach(el -> el.setExpanded(true));

        // 更新树结构
        if (inputValue instanceof JsonWrapper) {
            createSubtreeForNode(node, inputValue);
        } else {
            node.setValue(inputValue);
        }

        // 更新树结构
        filterableTree.updateStructure();

        UIUtils.repaintComponent(tree);
    }

    /**
     * 创建子树结构
     */
    private void createSubtreeForNode(JsonNode parentNode, Object jsonValue) {
        // 1. 移除现有子节点
        parentNode.removeAllChildren();

        // 2. 设置新值
        parentNode.setValue(jsonValue);

        // 3. 根据新值类型创建子树
        if (jsonValue instanceof ObjectWrapper) {
            // 处理 JSON 对象
            ObjectWrapper jsonObject = (ObjectWrapper) jsonValue;
            parentNode.setNodeType(JsonTreeNodeType.JSONObject);
            parentNode.setSize(jsonObject.size());

            // 创建子节点
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                JsonNode childNode = createChildNode(parentNode, entry.getKey(), entry.getValue());
                parentNode.add(childNode);
            }

        } else if (jsonValue instanceof ArrayWrapper) {
            // 处理 JSON 数组
            ArrayWrapper jsonArray = (ArrayWrapper) jsonValue;
            parentNode.setNodeType(JsonTreeNodeType.JSONArray);
            parentNode.setSize(jsonArray.size());

            // 创建子节点
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonNode childNode = createChildNode(parentNode, "item" + i, jsonArray.get(i));
                parentNode.add(childNode);
            }
        }

        // 5. 展开此节点
        parentNode.setExpanded(true);
    }

    /**
     * 创建子节点
     */
    private JsonNode createChildNode(JsonNode parent, String key, Object value) {
        JsonNode childNode = new JsonNode(key);
        childNode.setJsonPath(parent.getJsonPath() + "." + key).setParent(parent);

        // 父节点类型是否为数组类型
        boolean isParentNodeArrayType = JsonTreeNodeType.JSONArray == parent.getNodeType();

        if (value instanceof ObjectWrapper) {
            // 嵌套对象
            childNode.setNodeType(isParentNodeArrayType ? JsonTreeNodeType.JSONObjectElement : JsonTreeNodeType.JSONObject);
            childNode.setSize(((ObjectWrapper) value).size());
            childNode.setValue(value);

            // 递归创建子树
            for (Map.Entry<String, Object> entry : ((ObjectWrapper) value).entrySet()) {
                JsonNode grandChild = createChildNode(childNode, entry.getKey(), entry.getValue());
                childNode.add(grandChild);
            }

        } else if (value instanceof ArrayWrapper) {
            // 嵌套数组
            childNode.setNodeType(isParentNodeArrayType ? JsonTreeNodeType.JSONArrayElementArray : JsonTreeNodeType.JSONArray);
            childNode.setSize(((ArrayWrapper) value).size());
            childNode.setValue(value);

            // 递归创建子树
            ArrayWrapper jsonArray = (ArrayWrapper) value;
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonNode grandChild = createChildNode(childNode, "item" + i, jsonArray.get(i));
                childNode.add(grandChild);
            }

        } else {
            // 基本类型
            childNode.setNodeType(isParentNodeArrayType ? JsonTreeNodeType.JSONArrayElement : JsonTreeNodeType.JSONObjectProperty);
            childNode.setValue(value);
        }

        return childNode;
    }

}
