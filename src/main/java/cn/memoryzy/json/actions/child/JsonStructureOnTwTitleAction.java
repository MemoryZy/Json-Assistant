package cn.memoryzy.json.actions.child;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonStructureWindow;
import cn.memoryzy.json.ui.JsonWindow;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

public class JsonStructureOnTwTitleAction extends AnAction {
    private final Project project;
    private final JsonWindow window;

    public JsonStructureOnTwTitleAction(Project project, JsonWindow window) {
        super(JsonAssistantBundle.message("action.json.structure.text"), null, JsonAssistantIcons.Structure.STRUCTURE);

        this.project = project;
        this.window = window;
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
        new JsonStructureWindow(project, json).show();
    }

    public static boolean structuringUpdate(String text) {
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);
        return StrUtil.isNotBlank(jsonStr);
    }

}