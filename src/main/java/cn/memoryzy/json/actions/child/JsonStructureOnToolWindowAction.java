package cn.memoryzy.json.actions.child;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonStructureDialog;
import cn.memoryzy.json.ui.JsonViewWindow;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

public class JsonStructureOnToolWindowAction extends DumbAwareAction {
    private final JsonViewWindow window;

    public JsonStructureOnToolWindowAction(JsonViewWindow window, ToolWindowEx toolWindowEx) {
        super(JsonAssistantBundle.messageOnSystem("action.json.structure.text"),
                JsonAssistantBundle.messageOnSystem("action.json.structure.on.tw.title.description"),
                JsonAssistantIcons.Structure.STRUCTURE);

        this.window = window;

        registerCustomShortcutSet(CustomShortcutSet.fromString("alt T"), toolWindowEx.getComponent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        String text = StrUtil.trim(window.getJsonContent());
        structuring(text);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        String text = StrUtil.trim(window.getJsonContent());
        e.getPresentation().setEnabled(structuringUpdate(text));
    }

    public static void structuring(String text) {
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);
        JSONConfig jsonConfig = JSONConfig.create().setIgnoreNullValue(false);
        JsonStructureDialog dialog = new JsonStructureDialog(JSONUtil.parse(jsonStr, jsonConfig));
        ApplicationManager.getApplication().invokeLater(dialog::show);
    }

    public static boolean structuringUpdate(String text) {
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);
        return StrUtil.isNotBlank(jsonStr);
    }

}