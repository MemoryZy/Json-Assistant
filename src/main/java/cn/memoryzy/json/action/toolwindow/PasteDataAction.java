package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.event.RegisterClipboardUsageEvent;
import cn.memoryzy.json.model.strategy.ClipboardTextConverter;
import cn.memoryzy.json.model.strategy.clipboard.Json5ConversionStrategy;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionContext;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Memory
 * @since 2025/6/23
 */
public class PasteDataAction extends DumbAwareAction implements CustomComponentAction, UpdateInBackground {

    public PasteDataAction() {
        super(JsonAssistantBundle.messageOnSystem("action.paste.data.text"), null, AllIcons.Actions.MenuPaste);
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        ActionButton button = new ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE) {
            @Override
            protected void updateToolTipText() {
                HelpTooltip.dispose(this);

                // String previewText = PlatformUtil.isChineseLocale() ? "预览：" : "Preview:";
                // String description = JsonAssistantBundle.messageOnSystem("tooltip.paste.data.text");
                // String jsonText = truncateJsonPreview(StrUtil.trim(PlatformUtil.getClipboard()));
                // // String jsonText = "";
                //
                // // 创建带视觉分隔的JSON预览
                // String jsonPreview = MessageFormat.format("<html><p>{0}</p>" +
                //         "<div style='border-top:1px solid #e0e0e0; margin:10px 0; padding-top:10px;'>" +
                //         "<div style='color:#707070; font-size:0.9em; margin-bottom:4px;'>{1}</div>" +
                //         // "<div style='margin:10px 0; border-top:1px dashed #cccccc;'>" +
                //         // "<div style='color:#787878; font-weight:bold; margin-bottom:6px; margin-top:9px;'>{1}</div>" +
                //         "<pre style='background:#f8f9fa; padding:8px; border-radius:4px; margin:0; max-height:200px; overflow:hidden;'>" +
                //         "{2}"+
                //         "</pre>" +
                //         "</div>" +
                //         "</html>", description, previewText, jsonText);

                // TODO 后续可以将此Tooltip改为自定义组件，鼠标放在按钮上时显示，鼠标离开时消失

                // noinspection DialogTitleCapitalization
                new HelpTooltip()
                        .setTitle(getTemplatePresentation().getText())
                        .setDescription(JsonAssistantBundle.messageOnSystem("tooltip.paste.data.text"))
                        // .setPreviewText(StrUtil.trim(PlatformUtil.getClipboard()))
                        .installOn(this);
            }
        };

        button.setBorder(JBUI.Borders.empty(1, 2));
        return button;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = PlatformUtil.getEditor(e.getDataContext());

        String clipboard = StrUtil.trim(PlatformUtil.getClipboard());
        ClipboardTextConversionContext context = new ClipboardTextConversionContext();
        String processedText = ClipboardTextConverter.applyConversionStrategies(context, clipboard);
        if (StrUtil.isBlank(processedText)) return;

        JsonWrapper wrapper;
        if (context.getStrategy() instanceof Json5ConversionStrategy) {
            wrapper = Json5Util.parse(clipboard);
            processedText = Json5Util.formatJson5WithComment(clipboard);
        } else {
            wrapper = JsonUtil.parse(processedText);
            processedText = JsonUtil.formatJson(processedText);
        }

        // 空数据不处理
        if (null == wrapper || wrapper.noItems()) return;

        // 设置文本
        PlatformUtil.safeSetDocumentText(getEventProject(e), editor.getDocument(), processedText);
        // 注册剪贴板使用记录
        String hash = JsonAssistantUtil.calculateSHA256(clipboard);
        ApplicationManager.getApplication().getMessageBus().syncPublisher(RegisterClipboardUsageEvent.TOPIC).accept(editor, hash);
    }


    // 截取预览的逻辑
    private String truncateJsonPreview(String fullJson) {
        int MAX_LINES = 18;          // 关键行数限制
        int MAX_CHARS_PER_LINE = 60; // 单行最大字符数
        int TOTAL_CHARS = 500;       // 字符总数限制

        // 行数截断优先级 > 总字符截断
        String[] lines = fullJson.split("\\r?\\n");
        if (lines.length > MAX_LINES) {
            StringBuilder sb = new StringBuilder();
            int charCount = 0;

            // 优先保留前 N 行完整内容
            for (int i = 0; i < MAX_LINES && charCount < TOTAL_CHARS; i++) {
                String line = lines[i];
                if (line.length() > MAX_CHARS_PER_LINE) {
                    line = line.substring(0, MAX_CHARS_PER_LINE) + "...";
                }
                sb.append(line).append("\n");
                charCount += line.length();
            }

            // 添加截断提示
            sb.append("\n    ....... ");

            return sb.toString();
        }

        // 字符数截断兜底
        if (fullJson.length() > TOTAL_CHARS) {
            return fullJson.substring(0, TOTAL_CHARS) + "  ......";
        }

        return fullJson;
    }
}
