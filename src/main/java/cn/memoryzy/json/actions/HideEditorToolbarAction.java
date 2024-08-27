package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.JsonAssistantPlugin;
import cn.memoryzy.json.extensions.JsonViewerEditorFloatingProvider;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.Key;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/26
 */
public class HideEditorToolbarAction extends DumbAwareAction {

    public static final Key<Boolean> PERMANENTLY_HIDE_KEY = Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".permanently.hide");

    private final JsonViewerEditorFloatingProvider provider;

    public HideEditorToolbarAction() {
        super();
        this.provider = JsonViewerEditorFloatingProvider.getInstance();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.editor.toolbar.hide.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.editor.toolbar.hide.description"));
        presentation.setIcon(AllIcons.Actions.Close);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 在这里关闭toolbar后，属于主动关闭，那该窗口就不再启用了
        Content selectedContent = JsonAssistantUtil.getSelectedContent(JsonAssistantUtil.getJsonViewToolWindow(e.getProject()));
        provider.permanentlyHideToolbarComponent(selectedContent);
    }
}
