package cn.memoryzy.json.action.deserializer;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.ui.popup.KeepingPopupOpenAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/12/26
 */
public class JacksonToggleAction extends ToggleAction implements KeepingPopupOpenAction {

    private final DeserializerState deserializerState;

    public JacksonToggleAction() {
        super(JsonAssistantBundle.messageOnSystem("popup.deserializer.jackson.text"), null, null);
        this.deserializerState = JsonAssistantPersistentState.getInstance().deserializerState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializerState.jacksonAnnotation;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializerState.jacksonAnnotation = state;
    }
}