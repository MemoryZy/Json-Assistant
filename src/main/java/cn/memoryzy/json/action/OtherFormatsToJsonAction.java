package cn.memoryzy.json.action;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.formats.ActionInfo;
import cn.memoryzy.json.model.strategy.formats.context.AbstractConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.context.ConversionProcessorContext;
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
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = PlatformUtil.getEditor(e);
        // 使用多策略处理文本
        ConversionProcessorContext context = new ConversionProcessorContext();
        String processedText = ConversionProcessorContext.applyProcessors(context, editor);
        if (StrUtil.isNotBlank(processedText)) {
            TextTransformUtil.applyProcessedTextToDocument(
                    e.getProject(),
                    editor,
                    processedText,
                    context.getProcessor(),
                    // 创建新窗口展示转换完成的文本
                    false);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = false;
        Presentation presentation = e.getPresentation();
        Editor editor = PlatformUtil.getEditor(e);

        if (Objects.nonNull(getEventProject(e)) && Objects.nonNull(editor)) {
            ConversionProcessorContext context = new ConversionProcessorContext();
            if (ConversionProcessorContext.processMatching(context, editor)) {
                enabled = true;
                updateActionIfNeeded(presentation, context.getProcessor());
            }
        }

        presentation.setEnabledAndVisible(enabled);
    }


    private void updateActionIfNeeded(Presentation presentation, AbstractConversionProcessor processor) {
        ActionInfo actionInfo = processor.getActionInfo();
        String actionName = actionInfo.getActionName();
        String actionDescription = actionInfo.getActionDescription();
        Icon actionIcon = actionInfo.getActionIcon();

        String text = presentation.getText();
        String description = presentation.getDescription();
        Icon icon = presentation.getIcon();

        if (!Objects.equals(actionName, text)) presentation.setText(actionName);
        if (!Objects.equals(actionDescription, description)) presentation.setDescription(actionDescription);
        if (!Objects.equals(actionIcon, icon)) presentation.setIcon(actionIcon);
    }

}
