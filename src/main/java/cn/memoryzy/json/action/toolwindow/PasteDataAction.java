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

                // TODO 搞个预览  bundle tooltip.paste.data.text

                // noinspection DialogTitleCapitalization
                new HelpTooltip()
                        .setTitle(getTemplatePresentation().getText())
                        .setDescription("<p>转换并粘贴</p>\n" +
                                "<pre><code>{\n" +
                                "  // 登机口性质：D纯国内，I纯国际，B国内和国际\n" +
                                "  \"gateType\": \"I\",\n" +
                                "  // 国内值机人数\n" +
                                "  \"checkinCountD\": null,\n" +
                                "  // 国内登机人数\n" +
                                "  \"boardingCountD\": null,\n" +
                                "  // 国际值机人数\n" +
                                "  \"checkinCountI\": \"--\",\n" +
                                "  // 国际登机人数\n" +
                                "  \"boardingCountI\": \"300\",</code></pre>")
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
}
