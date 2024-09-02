package cn.memoryzy.json.extension;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.toolwindow.HideEditorToolbarAction;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.model.LimitedList;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.ui.component.editor.FoldingLanguageTextEditor;
import cn.memoryzy.json.util.JsonAssistantUtil;
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

    private final DisposableWrapperList<Pair<Content, FloatingToolbarComponent>> toolbarComponents = new DisposableWrapperList<>();

    public JsonViewerEditorFloatingProvider() {
        super(ActionHolder.EDITOR_TOOLBAR_GROUP_ID);
    }

    public static JsonViewerEditorFloatingProvider getInstance() {
        return FloatingToolbarProvider.Companion.getEP_NAME().findExtensionOrFail(JsonViewerEditorFloatingProvider.class);
    }

    @Override
    public void register(@NotNull DataContext dataContext, @NotNull FloatingToolbarComponent component, @NotNull Disposable parentDisposable) {
        Project project = dataContext.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            component.scheduleHide();
            return;
        }

        LimitedList<String> history = JsonViewerHistoryState.getInstance(project).getHistory();
        if (CollUtil.isEmpty(history)) {
            component.scheduleHide();
            return;
        }

        ToolWindow toolWindow = JsonAssistantUtil.getJsonViewToolWindow(project);
        Content selectedContent = JsonAssistantUtil.getSelectedContent(toolWindow);

        Editor editor = dataContext.getData(CommonDataKeys.EDITOR);
        if (editor == null || selectedContent == null) {
            component.scheduleHide();
            return;
        }

        String userData = editor.getUserData(FoldingLanguageTextEditor.PLUGIN_EDITOR_KEY);
        if (!Objects.equals(JsonAssistantPlugin.PLUGIN_ID_NAME, userData)) {
            component.scheduleHide();
            return;
        }

        // 判断是否为永久关闭的内容
        if (isPermanentlyHide(selectedContent)) {
            component.scheduleHide();
            return;
        }

        putToolbarComponent(Pair.pair(selectedContent, component));

        String text = editor.getDocument().getText();
        if (StrUtil.isNotBlank(text)) {
            component.scheduleHide();
        } else {
            component.scheduleShow();
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

    private void putToolbarComponent(Pair<Content, FloatingToolbarComponent> pair) {
        if (toolbarComponents.stream().noneMatch(el -> Objects.equals(el.getFirst(), pair.getFirst()))) {
            toolbarComponents.add(pair);
        }
    }

    public void hideToolbarComponent(Content content) {
        for (Pair<Content, FloatingToolbarComponent> pair : toolbarComponents) {
            if (Objects.equals(pair.getFirst(), content)) {
                pair.getSecond().scheduleHide();
            }
        }
    }

    public void permanentlyHideToolbarComponent(Content content) {
        for (Pair<Content, FloatingToolbarComponent> pair : toolbarComponents) {
            Content first = pair.getFirst();
            if (Objects.equals(first, content)) {
                first.putUserData(HideEditorToolbarAction.PERMANENTLY_HIDE_KEY, true);
                pair.getSecond().scheduleHide();
            }
        }
    }

    public boolean isPermanentlyHide(Content content) {
        for (Pair<Content, FloatingToolbarComponent> pair : toolbarComponents) {
            Content first = pair.getFirst();
            if (Objects.equals(first, content)) {
                Boolean userData = first.getUserData(HideEditorToolbarAction.PERMANENTLY_HIDE_KEY);
                return Boolean.TRUE.equals(userData);
            }
        }

        return false;
    }

    public void showToolbarComponent(Content content) {
        for (Pair<Content, FloatingToolbarComponent> pair : toolbarComponents) {
            Content first = pair.getFirst();
            if (Objects.equals(first, content)) {
                Boolean userData = first.getUserData(HideEditorToolbarAction.PERMANENTLY_HIDE_KEY);
                if (!Boolean.TRUE.equals(userData)) {
                    pair.getSecond().scheduleShow();
                }
            }
        }
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
            String oriDocumentText = event.getDocument().getText();

            // 原本的文本为空 && 新增的文本不为空 ==> 第一次添加文本
            if (StrUtil.isBlank(oriDocumentText) && StrUtil.isNotBlank(newFragment)) {
                Content selectedContent = JsonAssistantUtil.getSelectedContent(JsonAssistantUtil.getJsonViewToolWindow(project));
                if (selectedContent != null) {
                    provider.hideToolbarComponent(selectedContent);
                }
            }
        }

        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            String nowDocumentText = event.getDocument().getText();

            // 现在的文本为空 && 原先的文本不为空 ==> 文本全部删除，编辑器为空
            if (StrUtil.isBlank(nowDocumentText)) {
                Content selectedContent = JsonAssistantUtil.getSelectedContent(JsonAssistantUtil.getJsonViewToolWindow(project));
                if (selectedContent != null) {
                    provider.showToolbarComponent(selectedContent);
                }
            }
        }
    }
}
