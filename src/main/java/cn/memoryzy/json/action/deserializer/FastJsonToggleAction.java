package cn.memoryzy.json.action.deserializer;

import cn.memoryzy.json.service.persistent.state.v2.DeserializationState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/12/26
 */
public class FastJsonToggleAction extends ToggleAction implements UpdateInBackground {

    private final DeserializationState deserializationState;

    public FastJsonToggleAction(DeserializationState deserializationState) {
        super("@JSONField (FastJSON)", null, null);
        this.deserializationState = deserializationState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializationState.isEnableFastJsonAnnotation();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializationState.setEnableFastJsonAnnotation(state);
    }

}