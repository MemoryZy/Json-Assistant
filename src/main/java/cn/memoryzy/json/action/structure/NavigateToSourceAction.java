package cn.memoryzy.json.action.structure;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.EditorContext;
import cn.memoryzy.json.ui.tree.JsonTreeNode;
import cn.memoryzy.json.util.JsonPsiLocator;
import cn.memoryzy.json.util.Notifications;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.json.psi.JsonElement;
import com.intellij.json.psi.JsonValue;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Memory
 * @since 2025/7/22
 */
public class NavigateToSourceAction extends DumbAwareAction implements UpdateInBackground {

    private final Tree tree;
    private final AtomicReference<EditorContext> editorContextReference;

    public NavigateToSourceAction(Tree tree, AtomicReference<EditorContext> editorContextReference) {
        super(JsonAssistantBundle.messageOnSystem("action.locateSource.text"), JsonAssistantBundle.messageOnSystem("action.locateSource.description"), null);
        this.tree = tree;
        this.editorContextReference = editorContextReference;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        // 获取选中的树节点
        JsonTreeNode node = getSelectedNode();
        // 编辑器上下文
        EditorContext editorContext = editorContextReference.get();
        // 获取 JSON 整体对应的 PSI 文件
        PsiFile effectivePsiFile = editorContext.getEffectivePsiFile();
        // 定位到对应的 PSI 元素
        Pair<JsonElement, JsonValue> elementPair = JsonPsiLocator.locateByPath(effectivePsiFile, node.getJsonPath());
        if (elementPair == null) {
            Notifications.showNotification("Json Assistant", JsonAssistantBundle.messageOnSystem("error.content.changed.content"), NotificationType.WARNING, project);
            return;
        }

        // 打开编辑器 TODO 无法打开并焦点对应的编辑器组件
        Editor editor = editorContext.getEditor();
        IdeFocusManager.findInstance().requestFocus(editor.getContentComponent(), true);


    }

    @Nullable
    private JsonTreeNode getSelectedNode() {
        return (JsonTreeNode) UIUtils.getSelectedNode(tree);
    }
}
