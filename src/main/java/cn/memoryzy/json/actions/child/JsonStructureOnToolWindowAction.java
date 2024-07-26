package cn.memoryzy.json.actions.child;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonStructureDialog;
import cn.memoryzy.json.ui.JsonViewWindow;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

public class JsonStructureOnToolWindowAction extends DumbAwareAction {
    private final Project project;
    private final JsonViewWindow window;

    public JsonStructureOnToolWindowAction(Project project, JsonViewWindow window, ToolWindowEx toolWindowEx) {
        super(JsonAssistantBundle.messageOnSystem("action.json.structure.text"),
                JsonAssistantBundle.messageOnSystem("action.json.structure.on.tw.title.description"),
                JsonAssistantIcons.Structure.STRUCTURE);

        this.project = project;
        this.window = window;

        registerCustomShortcutSet(CustomShortcutSet.fromString("alt T"), toolWindowEx.getComponent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        String text = StrUtil.trim(window.getJsonContent());
        structuring(text, project);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        String text = StrUtil.trim(window.getJsonContent());
        e.getPresentation().setEnabled(structuringUpdate(text));
    }

    public static void structuring(String text, Project project) {
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);

        // 文档输入后检测Json数组，输出数量
        JSON json = JSONUtil.parse(jsonStr, JSONConfig.create().setIgnoreNullValue(false));
        new JsonStructureDialog(project, json).show();
    }

    public static boolean structuringUpdate(String text) {
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);
        return StrUtil.isNotBlank(jsonStr);
    }

}