package cn.memoryzy.json.actions;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.group.JsonProcessingPopupGroup;
import cn.memoryzy.json.utils.JsonUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/7/2
 */
public class JsonProcessingAction extends AnAction {

    public JsonProcessingAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.processing.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.processing.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new JsonProcessingPopupGroup().actionPerformed(e);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        String content = PlatformUtil.getEditorContent(e);
        String jsonStr = (JsonUtil.isJsonStr(content)) ? content : JsonUtil.extractJsonStr(content);
        e.getPresentation().setEnabledAndVisible(StrUtil.isNotBlank(jsonStr));
    }

}
