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
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/23
 */
public class PasteDataAction extends DumbAwareAction implements UpdateInBackground {

    public PasteDataAction() {
        super(JsonAssistantBundle.messageOnSystem("action.paste.data.text"), JsonAssistantBundle.messageOnSystem("action.paste.data.description"), AllIcons.Actions.MenuPaste);
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
