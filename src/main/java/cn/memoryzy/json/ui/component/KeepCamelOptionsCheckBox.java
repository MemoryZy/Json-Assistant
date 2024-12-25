package cn.memoryzy.json.ui.component;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import com.intellij.ui.components.JBCheckBox;

/**
 * @author Memory
 * @since 2024/12/25
 */
public class KeepCamelOptionsCheckBox extends JBCheckBox implements OptionsCheckBox {

    private final DeserializerState deserializerState;

    public KeepCamelOptionsCheckBox(DeserializerState deserializerState) {
        super(JsonAssistantBundle.messageOnSystem("popup.deserializer.keepCamel.text"), deserializerState.keepCamelCase);
        this.deserializerState = deserializerState;
    }

    @Override
    public boolean isFeatureEnabled() {
        return true;
    }

    @Override
    public void performed() {
        deserializerState.keepCamelCase = isSelected();
    }
}
