package cn.memoryzy.json.action.deserializer;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import cn.memoryzy.json.ui.icon.ToggleIcon;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.ui.popup.KeepingPopupOpenAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/12/26
 */
public class FastJsonToggleAction extends ToggleAction implements KeepingPopupOpenAction {
    private final ToggleIcon icon;
    private final DeserializerState deserializerState;

    public FastJsonToggleAction() {
        super(JsonAssistantBundle.messageOnSystem("popup.deserializer.fastJson.text"), null, null);
        this.deserializerState = JsonAssistantPersistentState.getInstance().deserializerState;
        this.icon = new ToggleIcon(deserializerState.fastJsonAnnotation);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializerState.fastJsonAnnotation;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializerState.fastJsonAnnotation = state;
        updateIcon();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        updateIcon();
        e.getPresentation().setIcon(icon);
    }

    private void updateIcon() {
        icon.prepare(deserializerState.fastJsonAnnotation);
    }

}