package cn.memoryzy.json.ui.component;

import cn.memoryzy.json.constant.DependencyConstant;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import cn.memoryzy.json.util.JavaUtil;
import com.intellij.openapi.module.Module;
import com.intellij.ui.components.JBCheckBox;

/**
 * @author Memory
 * @since 2025/1/18
 */
public class GetterLombokOptionsCheckBox  extends JBCheckBox implements OptionsCheckBox{

    private final Module module;
    private final DeserializerState deserializerState;

    public GetterLombokOptionsCheckBox(Module module, DeserializerState deserializerState) {
        super("@Getter (Lombok)", deserializerState.getterLombokAnnotation);
        this.module = module;
        this.deserializerState = deserializerState;
    }

    @Override
    public boolean isFeatureEnabled() {
        return JavaUtil.hasLibrary(module, DependencyConstant.LOMBOK_LIB);
    }

    @Override
    public void performed() {
        deserializerState.getterLombokAnnotation = isSelected();
    }
}
