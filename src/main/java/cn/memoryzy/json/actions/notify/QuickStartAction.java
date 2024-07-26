package cn.memoryzy.json.actions.notify;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.HTMLConstant;
import cn.memoryzy.json.constant.PluginDocument;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.util.Urls;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/26
 */
public class QuickStartAction extends DumbAwareAction {

    public QuickStartAction() {
        super(JsonAssistantBundle.messageOnSystem("action.quick.start.text"),
                JsonAssistantBundle.messageOnSystem("action.quick.start.description"),
                AllIcons.General.Web);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String url = PluginDocument.OVERVIEW;
        boolean darkTheme = UIUtil.isUnderDarcula();
        Map<String, String> parameters = darkTheme ? Map.of("theme", "dark") : Map.of("theme", "light");
        url = Urls.newFromEncoded(url).addParameters(parameters).toExternalForm();

        String timeoutContent = HTMLConstant.TIMEOUT_CONTENT
                .replace("__THEME__", darkTheme ? "theme-dark" : "")
                .replace("__TITLE__", JsonAssistantBundle.messageOnSystem("open.html.editor.timeout.title"))
                .replace("__MESSAGE__", JsonAssistantBundle.messageOnSystem("open.html.editor.timeout.message"))
                .replace("__ACTION__", JsonAssistantBundle.messageOnSystem("open.html.editor.timeout.action", url));

        HTMLEditorProvider.openEditor(
                Objects.requireNonNull(e.getProject()),
                JsonAssistantBundle.messageOnSystem("action.quick.start.text"),
                url, timeoutContent);
    }

}