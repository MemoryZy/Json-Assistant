package cn.memoryzy.json.action.deserializer;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;

public class FastJsonToggleAction extends ToggleAction {

    private final DeserializerState deserializerState;

    public FastJsonToggleAction() {
        super(JsonAssistantBundle.messageOnSystem("action.deserializer.fastJson.text"));
        this.deserializerState = JsonAssistantPersistentState.getInstance().deserializerState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializerState.fastJsonAnnotation;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializerState.fastJsonAnnotation = state;
    }
}
