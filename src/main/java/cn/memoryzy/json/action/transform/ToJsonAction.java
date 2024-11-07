package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import cn.memoryzy.json.util.YamlUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/11/4
 */
public class ToJsonAction extends DumbAwareAction implements UpdateInBackground {

    public ToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json5.to.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json5.to.json.description"));
        presentation.setIcon(AllIcons.FileTypes.Json);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // TODO 待实现 json5 到 json
        DataContext dataContext = event.getDataContext();






        String yamlStr;

        yamlStr = YamlUtil.toYaml(
                GlobalJsonConverter.parseJson(
                        dataContext,
                        PlatformUtil.getEditor(dataContext)));


        TextTransformUtil.applyTextWhenNotWritable(getEventProject(event), yamlStr, FileTypeHolder.YAML);

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 确保当前是JSON5
        DataContext dataContext = e.getDataContext();
        e.getPresentation().setEnabledAndVisible(
                GlobalJsonConverter.validateEditorJson5(
                        getEventProject(e), PlatformUtil.getEditor(dataContext)));
    }
}
