package cn.memoryzy.json.action.deserializer;

import cn.memoryzy.json.service.persistent.state.v2.DeserializationState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/1/21
 */
public class FastJson2ToggleAction extends ToggleAction implements UpdateInBackground {

    private final DeserializationState deserializationState;

    public FastJson2ToggleAction(DeserializationState deserializationState) {
        super("@JSONField (FastJSON2)", null, null);
        this.deserializationState = deserializationState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializationState.isEnableFastJson2Annotation();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializationState.setEnableFastJson2Annotation(state);
    }

}
