package cn.memoryzy.json.group;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;

/**
 * @author Memory
 * @since 2024/6/29
 */
public class JsonProcessingGroup extends DefaultActionGroup {
    public JsonProcessingGroup() {
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("group.json.processing.text"));
    }
}
