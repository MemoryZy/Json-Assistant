package cn.memoryzy.json.extensions;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constants.JsonAssistantPlugin;
import cn.memoryzy.json.ui.basic.editor.FoldingLanguageTextEditor;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.toolbar.floating.AbstractFloatingToolbarProvider;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider;
import com.intellij.openapi.project.Project;
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
public class JsonViewerEditorFloatingProvider extends AbstractFloatingToolbarProvider {

    public final DisposableWrapperList<Pair<Content, FloatingToolbarComponent>> toolbarComponents = new DisposableWrapperList<>();

    public JsonViewerEditorFloatingProvider() {
        super("JsonAssistant.Group.EditorToolbarGroup");
    }

    public static JsonViewerEditorFloatingProvider getInstance() {
        return FloatingToolbarProvider.Companion.getEP_NAME().findExtensionOrFail(JsonViewerEditorFloatingProvider.class);
    }

    @Override
    public void register(@NotNull DataContext dataContext, @NotNull FloatingToolbarComponent component, @NotNull Disposable parentDisposable) {
        ToolWindow toolWindow = JsonAssistantUtil.getJsonViewToolWindow(dataContext.getData(CommonDataKeys.PROJECT));
        Content selectedContent = JsonAssistantUtil.getSelectedContent(toolWindow);

        Editor editor = dataContext.getData(CommonDataKeys.EDITOR);
        if (editor == null || selectedContent == null) return;

        String userData = editor.getUserData(FoldingLanguageTextEditor.PLUGIN_EDITOR_KEY);
        if (!Objects.equals(JsonAssistantPlugin.PLUGIN_ID_NAME, userData)) return;

        putToolbarComponent(Pair.pair(selectedContent, component));

        String text = editor.getDocument().getText();
        if (StrUtil.isNotBlank(text)) {
            component.scheduleHide();
        } else {
            component.scheduleShow();
        }
    }

    private void putToolbarComponent(Pair<Content, FloatingToolbarComponent> pair) {
        if (toolbarComponents.stream().noneMatch(el -> Objects.equals(el.getFirst(), pair.getFirst()))) {
            toolbarComponents.add(pair);
        }
    }

    private void hideToolbarComponent(Content content) {
        toolbarComponents.stream().filter(el -> Objects.equals(el.getFirst(), content)).findFirst().ifPresent(pair -> pair.getSecond().scheduleHide());
    }

    private void removeToolbarComponent(Content content) {
        toolbarComponents.removeIf(el -> Objects.equals(el.getFirst(), content));
    }

    @Override
    public boolean getAutoHideable() {
        return false;
    }

    public static class ContentDisposable implements Disposable {
        private final Content content;
        private final JsonViewerEditorFloatingProvider provider;

        public ContentDisposable(Content content) {
            this.content = content;
            this.provider = getInstance();
        }

        @Override
        public void dispose() {
            provider.removeToolbarComponent(content);
        }
    }

    public static class DocumentListenerImpl implements DocumentListener {
        private final Project project;
        private final JsonViewerEditorFloatingProvider provider;

        public DocumentListenerImpl(Project project) {
            this.project = project;
            this.provider = getInstance();
        }

        @Override
        public void beforeDocumentChange(@NotNull DocumentEvent event) {
            CharSequence newFragment = event.getNewFragment();
            String documentText = event.getDocument().getText();

            // 第一次新增文本时才进入取消显示逻辑
            // 当输入文本时关闭窗口，但是删除全部文本时应该开启
            if (StrUtil.isBlank(documentText)) {


            }
        }

        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            // 输入字符后，隐藏Toolbar
            if (StrUtil.isNotBlank(event.getNewFragment())) {
                Content selectedContent = JsonAssistantUtil.getSelectedContent(JsonAssistantUtil.getJsonViewToolWindow(project));
                if (selectedContent != null) {
                    provider.hideToolbarComponent(selectedContent);
                }
            }
        }
    }
}
