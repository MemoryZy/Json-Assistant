package cn.memoryzy.json.action.structure;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.state.StructureState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CheckedActionGroup;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/4/7
 */
public class DisplayNodePathAction extends ToggleAction implements CheckedActionGroup, DumbAware {

    private final StructureState structureState;

    public DisplayNodePathAction(StructureState structureState) {
        super(JsonAssistantBundle.messageOnSystem("action.displayNodePath.text"), JsonAssistantBundle.messageOnSystem("action.displayNodePath.description"), JsonAssistantIcons.Structure.PATH);
        this.structureState = structureState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return structureState.displayNodePath;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        structureState.displayNodePath = state;
    }
}
