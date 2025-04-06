package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.StructureConfig;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.UIManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/12/16
 */
public class JsonTreeEditorComponentProvider {

    private final JsonStructureComponentProvider componentProvider;

    public JsonTreeEditorComponentProvider(Project project, String content) {
        this.componentProvider = new JsonStructureComponentProvider(getJsonWrapper(content), UIManager.getWindowComponent(project), StructureConfig.of(false));
    }

    public JComponent getComponent() {
        return componentProvider.getTreeComponent();
    }

    public JComponent getPreferredFocusedComponent() {
        return componentProvider.getTree();
    }

    public void selectNotify(String content) {
        componentProvider.rebuildTree(getJsonWrapper(content));
    }

    private JsonWrapper getJsonWrapper(String content) {
        if (StrUtil.isBlank(content)) {
            return null;
        }

        if (JsonUtil.isJson(content)) {
            return JsonUtil.parse(content);
        }

        if (Json5Util.isJson5(content)) {
            return Json5Util.parseWithComment(content);
        }

        return null;
    }
}
