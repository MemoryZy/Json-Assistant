package cn.memoryzy.json.ui.component;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import cn.memoryzy.json.util.JavaUtil;
import com.intellij.openapi.module.Module;
import com.intellij.ui.components.JBCheckBox;

/**
 * @author Memory
 * @since 2024/12/25
 */
public class FastJson2OptionsCheckBox extends JBCheckBox implements OptionsCheckBox {

    private final Module module;
    private final DeserializerState deserializerState;

    public FastJson2OptionsCheckBox(Module module, DeserializerState deserializerState) {
        super(JsonAssistantBundle.messageOnSystem("popup.deserializer.fastJson2.text"), deserializerState.fastJson2Annotation);
        this.module = module;
        this.deserializerState = deserializerState;
    }

    @Override
    public boolean isFeatureEnabled() {
        return JavaUtil.hasFastJson2Lib(module);
    }

    @Override
    public void performed() {
        deserializerState.fastJson2Annotation = isSelected();
    }
}
