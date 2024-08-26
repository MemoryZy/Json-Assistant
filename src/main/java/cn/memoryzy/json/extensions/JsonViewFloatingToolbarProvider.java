package cn.memoryzy.json.extensions;

import cn.memoryzy.json.constants.JsonAssistantPlugin;
import cn.memoryzy.json.ui.basic.editor.FoldingLanguageTextEditor;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.toolbar.floating.AbstractFloatingToolbarProvider;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.util.containers.DisposableWrapperList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/26
 */
public class JsonViewFloatingToolbarProvider extends AbstractFloatingToolbarProvider {

    private final DisposableWrapperList<Pair<String, FloatingToolbarComponent>> toolbarComponents = new DisposableWrapperList<>();

    public JsonViewFloatingToolbarProvider() {
        super("json-assistant.xxx.group");
    }

    @Override
    public void register(@NotNull DataContext dataContext, @NotNull FloatingToolbarComponent component, @NotNull Disposable parentDisposable) {
        ToolWindow toolWindow = JsonAssistantUtil.getJsonViewToolWindow(dataContext.getData(CommonDataKeys.PROJECT));
        Content selectedContent = JsonAssistantUtil.getSelectedContent(toolWindow);

        Editor editor = dataContext.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;

        String userData = editor.getUserData(FoldingLanguageTextEditor.PLUGIN_EDITOR_KEY);
        if (!Objects.equals(JsonAssistantPlugin.PLUGIN_ID_NAME, userData)) return;

        toolbarComponents.add(Pair.pair(selectedContent.getDisplayName(), component));

        // 当

        String text = editor.getDocument().getText();
        // if (StrUtil.isNotBlank(text)) {
        //     component.scheduleHide();
        // } else {
        //     component.scheduleShow();
        // }

        component.scheduleShow();

        ExtensionPointName<FloatingToolbarProvider> pointName = FloatingToolbarProvider.Companion.getEP_NAME();
        JsonViewFloatingToolbarProvider provider = pointName.findExtensionOrFail(JsonViewFloatingToolbarProvider.class);

        System.out.println();
        // 在编辑器里加个监听，当已经输入了字符，就把操作隐藏
        // 看 /com/intellij/openapi/externalSystem/autoimport/ProjectRefreshFloatingProvider.kt:48
    }

    @Override
    public boolean getAutoHideable() {
        return false;
    }
}
