package cn.memoryzy.json.actions.child;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.icons.JsonAssistantIcons;
import cn.memoryzy.json.ui.JsonStructureWindow;
import cn.memoryzy.json.ui.JsonWindow;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JsonStructureAction extends AnAction {
    private final Project project;
    private final JsonWindow window;

    public JsonStructureAction(ToolWindow toolWindow, Project project, JsonWindow window) {
        super(JsonAssistantBundle.message("action.json.structure.text"), null, JsonAssistantIcons.Structure.STRUCTURE);

        this.project = project;
        this.window = window;

        JComponent component = toolWindow.getContentManager().getComponent();
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt S"), component);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        String text = StrUtil.trim(window.getJsonContent());
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);

        // 文档输入后检测Json数组，输出数量
        if (StrUtil.isNotBlank(jsonStr)) {
            if (JsonUtil.isJsonStr(jsonStr)) {
                JSON json = JSONUtil.parse(jsonStr, JSONConfig.create().setIgnoreNullValue(false));
                new JsonStructureWindow(project, json).show();
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        String text = StrUtil.trim(window.getJsonContent());
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);
        e.getPresentation().setEnabled(StrUtil.isNotBlank(jsonStr) && JsonUtil.isJsonStr(jsonStr));
    }
}