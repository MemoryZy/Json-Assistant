package cn.memoryzy.json.action.debug;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * @author Memory
 * @since 2025/3/28
 */
public class RuntimeObjectToJson5Action extends AnAction implements UpdateInBackground {

    public RuntimeObjectToJson5Action() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.serialize.json5.runtime.object.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.serialize.json5.runtime.object.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        RuntimeObjectToJsonAction.handleObjectReferenceResolution(e.getProject(), dataContext, el -> convertResult(el, dataContext), true);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // TODO 判断逻辑需要改，改为识别为JavaBean的，或者List包含JavaBean的才显示
        e.getPresentation().setEnabledAndVisible(RuntimeObjectToJsonAction.isEnabled(e.getDataContext()));
    }

    private String convertResult(Object result, DataContext dataContext) {
        if (result instanceof Map) {


        } else if (result instanceof List) {

        }

        return null;
    }
}
