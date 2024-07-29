package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.HyperLinks;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.util.Urls;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/26
 */
public class QuickStartAction extends DumbAwareAction {

    private static final Logger LOG = Logger.getInstance(QuickStartAction.class);

    public QuickStartAction() {
        super(JsonAssistantBundle.messageOnSystem("action.quick.start.text"),
                JsonAssistantBundle.messageOnSystem("action.quick.start.description"),
                AllIcons.General.Web);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String url = HyperLinks.OVERVIEW;
        boolean darkTheme = UIUtil.isUnderDarcula();
        Map<String, String> parameters = darkTheme ? Map.of("theme", "dark") : Map.of("theme", "light");
        url = Urls.newFromEncoded(url).addParameters(parameters).toExternalForm();


        if (PlatformUtil.canBrowseInHTMLEditor()) {
            String timeoutContent = null;
            try (InputStream html = QuickStartAction.class.getResourceAsStream("timeout.html")) {
                if (html != null) {
                    timeoutContent = new String(StreamUtil.readBytes(html), StandardCharsets.UTF_8)
                            .replace("__THEME__", darkTheme ? "theme-dark" : "")
                            .replace("__TITLE__", JsonAssistantBundle.messageOnSystem("open.html.editor.timeout.title"))
                            .replace("__MESSAGE__", JsonAssistantBundle.messageOnSystem("open.html.editor.timeout.message"))
                            .replace("__ACTION__", JsonAssistantBundle.messageOnSystem("open.html.editor.timeout.action", url));
                }
            } catch (IOException ex) {
                LOG.error(ex);
            }

            HTMLEditorProvider.openEditor(
                    Objects.requireNonNull(e.getProject()),
                    JsonAssistantBundle.messageOnSystem("action.quick.start.text"),
                    url, timeoutContent);
        } else {
            BrowserUtil.browse(url);
        }
    }

}