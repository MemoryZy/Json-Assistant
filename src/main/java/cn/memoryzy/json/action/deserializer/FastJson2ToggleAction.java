package cn.memoryzy.json.action.deserializer;

import cn.memoryzy.json.service.persistent.state.DeserializerState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/1/21
 */
public class FastJson2ToggleAction extends ToggleAction implements UpdateInBackground {

    private final DeserializerState deserializerState;

    public FastJson2ToggleAction(DeserializerState deserializerState) {
        super("@JSONField (FastJSON2)", null, null);
        this.deserializerState = deserializerState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializerState.fastJson2Annotation;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializerState.fastJson2Annotation = state;
    }

}
