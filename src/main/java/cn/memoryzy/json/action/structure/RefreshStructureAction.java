package cn.memoryzy.json.action.structure;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.JsonStructureComponentProvider;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2025/4/17
 */
public class RefreshStructureAction extends DumbAwareAction implements UpdateInBackground {

    private final VirtualFile file;
    private final JsonStructureComponentProvider provider;

    public RefreshStructureAction(JsonStructureComponentProvider provider, VirtualFile file) {
        super(JsonAssistantBundle.messageOnSystem("action.refresh.structure.text"), JsonAssistantBundle.messageOnSystem("action.refresh.structure.description"), JsonAssistantIcons.Structure.REFRESH);
        registerCustomShortcutSet(CustomShortcutSet.fromString("ctrl F5"), provider.getTreeComponent());
        this.provider = provider;
        this.file = file;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 获取 源文本，重新加载页面
        EditorEx editor = PlatformUtil.getEditor(getEventProject(e), file);
        String text = editor.getDocument().getText();

        JsonWrapper wrapper = JsonUtil.isJson(text) ? JsonUtil.parse(text) : Json5Util.parse(text);
        provider.rebuildTree(wrapper, 3);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = false;
        Project project = getEventProject(e);

        if (Objects.nonNull(project)) {
            EditorEx editor = PlatformUtil.getEditor(getEventProject(e), file);
            if (Objects.nonNull(editor)) {
                String text = editor.getDocument().getText();
                if (JsonUtil.isJson(text) || Json5Util.isJson5(text)) {
                    enabled = true;
                }
            }
        }

        e.getPresentation().setEnabled(enabled);
    }
}
