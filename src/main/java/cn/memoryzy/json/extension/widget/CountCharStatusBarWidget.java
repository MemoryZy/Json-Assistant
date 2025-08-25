package cn.memoryzy.json.extension.widget;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.ProjectEditorManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.BulkAwareDocumentListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author Memory
 * @since 2025/8/25
 */
public class CountCharStatusBarWidget extends EditorBasedWidget
        implements StatusBarWidget.TextPresentation, SelectionListener, BulkAwareDocumentListener.Simple, DumbAware {

    public static final String ID = "JsonAssistant.CountCharWidget";

    private String myText = "";
    private final Project project;
    private MergingUpdateQueue myQueue;

    public CountCharStatusBarWidget(@NotNull Project project) {
        super(project);
        this.project = project;
    }

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }

    @Override
    public @NotNull String getText() {
        return myText == null ? "" : myText;
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
        return "";
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
        myQueue = new MergingUpdateQueue("CountCharStatusBarWidget", 100, true, null, this);
        EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
        multicaster.addSelectionListener(this, this);
        multicaster.addDocumentListener(this, this);
    }

    @Override
    public void afterDocumentChange(@NotNull Document document) {
        Editor editor = EditorFactory.getInstance().editors(document)
                .filter(this::isFocusedEditor)
                .findFirst()
                .orElse(null);

        if (null == editor) {
            // 寻找自己的编辑器
            ProjectEditorManager editorManager = ProjectEditorManager.getInstance(project);
            editor = editorManager.getEditors().stream()
                    .filter(el -> el.getDocument().equals(document) && project.equals(el.getProject()))
                    .filter(this::isFocusedEditor)
                    .findFirst()
                    .orElse(null);
        }

        if (null != editor) updateText(editor);
    }

    @Override
    public void selectionChanged(@NotNull SelectionEvent e) {
        Editor editor = e.getEditor();
        if (isFocusedEditor(editor)) updateText(editor);
    }

    private boolean isFocusedEditor(Editor editor) {
        Component focusOwner = getFocusedComponent2();
        return focusOwner == editor.getContentComponent();
    }

    private void updateText(Editor editor) {
        myQueue.queue(Update.create(this, () -> {
            if (editor == null || editor.isDisposed()) {
                myText = "";
            } else {
                int totalChars = editor.getDocument().getTextLength();
                myText = JsonAssistantBundle.messageOnSystem("widget.count.char.prefix.name") + " " + totalChars;
                // myText = "Chars: " + totalChars;
            }

            if (myStatusBar != null) {
                myStatusBar.updateWidget(ID());
            }
        }));
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
}
