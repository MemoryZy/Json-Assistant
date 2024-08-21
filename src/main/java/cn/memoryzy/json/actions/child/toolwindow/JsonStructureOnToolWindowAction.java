package cn.memoryzy.json.actions.child.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

public class JsonStructureOnToolWindowAction extends DumbAwareAction implements UpdateInBackground {
    private final JsonViewerWindow window;

    public JsonStructureOnToolWindowAction(JsonViewerWindow window, ToolWindowEx toolWindow) {
        super(JsonAssistantBundle.messageOnSystem("action.json.structure.text"),
                JsonAssistantBundle.messageOnSystem("action.json.structure.on.tw.title.description"),
                JsonAssistantIcons.Structure.STRUCTURE);

        this.window = window;

        registerCustomShortcutSet(CustomShortcutSet.fromString("alt T"), toolWindow.getComponent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        String text = StrUtil.trim(window.getJsonContent());
        JsonAssistantUtil.showJsonStructureDialog(text);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        String text = StrUtil.trim(window.getJsonContent());
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);
        e.getPresentation().setEnabled(StrUtil.isNotBlank(jsonStr));
    }

}