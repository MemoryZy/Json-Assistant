package cn.memoryzy.json.action.deserializer;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/12/26
 */
public class KeepCamelToggleAction extends ToggleAction implements UpdateInBackground {

    private final DeserializerState deserializerState;

    public KeepCamelToggleAction(DeserializerState deserializerState) {
        super(JsonAssistantBundle.messageOnSystem("popup.deserializer.keepCamel.text"), null, null);
        this.deserializerState = deserializerState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializerState.keepCamelCase;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializerState.keepCamelCase = state;
    }
}