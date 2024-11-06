package cn.memoryzy.json.action;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.data.ActionData;
import cn.memoryzy.json.model.strategy.GlobalTextConverter;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class OtherFormatsToJsonAction extends DumbAwareAction implements UpdateInBackground {

    public OtherFormatsToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.other.formats.to.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.other.formats.to.json.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = PlatformUtil.getEditor(event.getDataContext());
        // 使用多策略处理文本
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String processedText = GlobalTextConverter.applyConversionProcessors(context, editor);
        if (StrUtil.isNotBlank(processedText)) {
            TextTransformUtil.applyProcessedTextToDocument(
                    getEventProject(event),
                    editor,
                    processedText,
                    context.getProcessor(),
                    // 创建新窗口展示转换完成的文本
                    false);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        boolean enabled = false;
        Presentation presentation = event.getPresentation();
        Editor editor = PlatformUtil.getEditor(event.getDataContext());

        if (Objects.nonNull(getEventProject(event)) && Objects.nonNull(editor)) {
            GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
            if (GlobalTextConverter.validateEditorText(context, editor)) {
                enabled = true;
                updateActionIfNeeded(presentation, context.getProcessor());
            }
        }

        presentation.setEnabledAndVisible(enabled);
    }


    private void updateActionIfNeeded(Presentation presentation, AbstractGlobalTextConversionProcessor processor) {
        ActionData actionData = processor.getActionData();
        String actionName = actionData.getActionName();
        String actionDescription = actionData.getActionDescription();
        Icon actionIcon = actionData.getActionIcon();

        String text = presentation.getText();
        String description = presentation.getDescription();
        Icon icon = presentation.getIcon();

        if (!Objects.equals(actionName, text)) presentation.setText(actionName);
        if (!Objects.equals(actionDescription, description)) presentation.setDescription(actionDescription);
        if (!Objects.equals(actionIcon, icon)) presentation.setIcon(actionIcon);
    }

}
