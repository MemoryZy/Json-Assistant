package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.EditorContext;
import cn.memoryzy.json.model.structure.StructureSetting;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.node.JsonTreeNode;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/12/16
 */
public class JsonTreeEditorComponentProvider {

    private final VirtualFile virtualFile;
    private final JsonStructureComponentProvider componentProvider;

    public JsonTreeEditorComponentProvider(Project project, VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
        this.componentProvider = new JsonStructureComponentProvider(
                null,
                UIUtils.getWindowComponent(project),
                new StructureSetting()
                        .setNeedBorder(false)
                        .setNeedToolbar(true)
                        .setExpandLevel(3)
                        .setNeedRefresh(true)
                        .setFile(virtualFile));
    }

    public JComponent getComponent() {
        return componentProvider.getTreeComponent();
    }

    public JComponent getPreferredFocusedComponent() {
        return componentProvider.getTree();
    }

    private JsonWrapper getJsonWrapper(String content) {
        if (StrUtil.isBlank(content)) {
            return null;
        }

        if (JsonUtil.isJson(content)) {
            return JsonUtil.parse(content);
        }

        if (Json5Util.isJson5(content)) {
            return Json5Util.parseWithComment(content);
        }

        return null;
    }

    public void compareAndRefresh(Project project, VirtualFile file) {
        EditorEx editor = PlatformUtil.getEditor(project, file);
        String text = Optional.ofNullable(editor)
                .map(EditorEx::getDocument)
                .map(DocumentEx::getText)
                .orElse(null);

        if (StrUtil.isBlank(text)) {
            text = PlatformUtil.getFileRealContent(project, virtualFile);
        }

        if (StrUtil.isBlank(text)) {
            return;
        }

        // 获取编辑器及文件上下文
        EditorContext editorContext = PlatformUtil.getEditorContext(project, editor);

        // 判断是否有子节点
        JsonWrapper newWrapper = getJsonWrapper(text);
        if (newWrapper == null) {
            return;
        }

        Tree tree = componentProvider.getTree();
        JsonTreeNode rootNode = (JsonTreeNode) tree.getModel().getRoot();
        int childCount = rootNode.getChildCount();
        if (childCount == 0) {
            // 初次加载
            componentProvider.rebuildTree(newWrapper, 3, editorContext);

        } else {
            // 结构相同无需操作，不同则重建树
            JsonWrapper oldWrapper = (JsonWrapper) rootNode.getValue();

            // 结构不同，重构树
            if (!Objects.equals(oldWrapper, newWrapper)) {
                componentProvider.rebuildTree(newWrapper, 3, editorContext);
            }
        }
    }
}
