package cn.memoryzy.json.ui.component;

import cn.memoryzy.json.service.persistent.state.DeserializerState;
import cn.memoryzy.json.util.JavaUtil;
import com.intellij.openapi.module.Module;
import com.intellij.ui.components.JBCheckBox;

/**
 * @author Memory
 * @since 2024/12/25
 */
public class FastJsonOptionsCheckBox extends JBCheckBox implements OptionsCheckBox {

    private final Module module;
    private final DeserializerState deserializerState;

    public FastJsonOptionsCheckBox(Module module, DeserializerState deserializerState) {
        super("@JSONField (FastJSON)", deserializerState.fastJsonAnnotation);
        this.module = module;
        this.deserializerState = deserializerState;
    }

    @Override
    public boolean isFeatureEnabled() {
        return JavaUtil.hasFastJsonLib(module);
    }

    @Override
    public void performed() {
        deserializerState.fastJsonAnnotation = isSelected();
    }

}
