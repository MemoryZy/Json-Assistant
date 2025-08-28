package cn.memoryzy.json.extension.widget;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.event.EditorCharChangedEvent;
import cn.memoryzy.json.model.event.EditorCountCharEvent;
import cn.memoryzy.json.service.persistent.HistoryManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.BulkAwareDocumentListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Memory
 * @since 2025/8/25
 */
public class CountCharStatusBarWidget extends EditorBasedWidget
        implements StatusBarWidget.TextPresentation, BulkAwareDocumentListener.Simple, PropertyChangeListener {

    public static final String ID = "JsonAssistant.CountCharWidget";

    private String text = "";
    private String tooltipText = "";
    private MergingUpdateQueue updateQueue;

    public CountCharStatusBarWidget(@NotNull Project project) {
        super(project);
        MessageBusConnection projectConnection = project.getMessageBus().connect(HistoryManager.getInstance(project));
        // 注册事件处理器
        projectConnection.subscribe(EditorCharChangedEvent.TOPIC, (EditorCharChangedEvent) this::update);
    }

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }

    @Override
    public @NotNull String getText() {
        return text == null ? "" : text;
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public float getAlignment() {
        return Component.CENTER_ALIGNMENT;
    }

    @Override
    public @Nullable String getTooltipText() {
        // return JsonAssistantBundle.messageOnSystem("widget.count.char.tooltip.prefix") + " " + myTooltipText;
        return tooltipText;
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
        updateQueue = new MergingUpdateQueue("CountCharStatusBarWidget", 100, true, null, this);
        EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
        multicaster.addDocumentListener(this, this);
        // 注册焦点监听器
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(SWING_FOCUS_OWNER_PROPERTY, this);
        // 自动移除监听器（防止内存泄漏）
        Disposer.register(this,
                () -> KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(SWING_FOCUS_OWNER_PROPERTY, this));
    }

    @Override
    public void afterDocumentChange(@NotNull Document document) {
        EditorFactory.getInstance().editors(document)
                .filter(this::isFocusedEditor)
                .findFirst()
                .ifPresent(this::updateText);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateText(getFocusedEditor());
    }

    private boolean isFocusedEditor(Editor editor) {
        Component focusOwner = getFocusedComponent2();
        return focusOwner == editor.getContentComponent();
    }

    private void updateText(Editor editor) {
        updateText(editor, null, 0);
    }

    private void updateText(Editor editor, VirtualFile virtualFile, int countChars) {
        updateQueue.queue(Update.create(this, () -> {
            if (editor == null || editor.isDisposed()) {
                text = "";
                tooltipText = "";
            } else {
                Document document = editor.getDocument();

                int totalChars = countChars;
                if (0 == totalChars) {
                    totalChars = document.getTextLength();
                }

                text = JsonAssistantBundle.messageOnSystem("widget.count.char.prefix.name") + " " + totalChars;

                VirtualFile file = virtualFile;
                if (null == file) {
                    file = FileDocumentManager.getInstance().getFile(document);
                }

                tooltipText = null == file ? "" : file.getName();
            }

            if (myStatusBar != null) {
                myStatusBar.updateWidget(ID());
            }
        }));
    }

    private void update(EditorCountCharEvent event) {
        // 判断是否是带焦点的编辑器
        Editor editor = event.getEditor();
        if (isFocusedEditor(editor)) {
            updateText(event.getEditor(), event.getFile(), event.getTotalChars());
        }
    }

    public @Nullable Component getFocusedComponent2() {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner == null) {
            IdeFocusManager focusManager = IdeFocusManager.getInstance(myProject);
            Window frame = focusManager.getLastFocusedIdeWindow();
            if (frame != null) {
                focusOwner = focusManager.getLastFocusedFor(frame);
            }
        }
        return focusOwner;
    }

    public @Nullable Editor getFocusedEditor() {
        Component component = getFocusedComponent2();
        Editor editor = component instanceof EditorComponentImpl ? ((EditorComponentImpl)component).getEditor() : getEditor();
        return editor != null && !editor.isDisposed() ? editor : null;
    }
}
