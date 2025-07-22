package cn.memoryzy.json.ui;

import cn.memoryzy.json.action.editor.CollapseAction;
import cn.memoryzy.json.action.editor.ExpandAction;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollBar;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Memory
 * @since 2025/7/21
 */
public class ExpandableEditorProvider {

    private final Project project;
    private final Editor collapsedEditor;

    public ExpandableEditorProvider(Project project) {
        this(project, PlainTextFileType.INSTANCE);
    }

    public ExpandableEditorProvider(Project project, FileType fileType) {
        this.project = project;
        this.collapsedEditor = createCollapseEditor(project, fileType);
    }

    public JComponent createComponent() {
        return collapsedEditor.getComponent();
    }


    private Editor createCollapseEditor(Project project, FileType fileType) {
        EditorEx editor = (EditorEx) PlatformUtil.createEditor(project, "Dummy", fileType, false, EditorKind.MAIN_EDITOR, "");
        editor.setOneLineMode(true);

        // ActionButton collapseButton = createCollapseButton(editor);
        // collapseButton.setBorder(JBUI.Borders.emptyLeft(2));
        // collapseButton.setOpaque(true);

        JLabel label = new JLabel(AllIcons.General.CollapseComponent) {{
            // setToolTipText(extension.getTooltip());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    setIcon(AllIcons.General.CollapseComponentHover);
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    setIcon(AllIcons.General.CollapseComponent);
                }

                @Override
                public void mouseClicked(MouseEvent event) {
                    // Runnable action = extension.getActionOnClick();
                    // if (action != null) action.run();
                }
            });
        }};

        label.setBorder(JBUI.Borders.emptyLeft(2));
        label.setOpaque(true);

        editor.getScrollPane().setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        editor.getScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        editor.getScrollPane().getVerticalScrollBar().add(JBScrollBar.LEADING, label);
        editor.getScrollPane().getVerticalScrollBar().setOpaque(true);

        return editor;
    }


    @NotNull
    private ActionButton createCollapseButton(Editor editor) {
        CollapseAction collapseAction = new CollapseAction(editor);
        Presentation presentation = new Presentation();
        presentation.copyFrom(collapseAction.getTemplatePresentation());
        return new ActionButton(collapseAction, presentation, ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);
    }

    @NotNull
    private ActionButton createExpandButton(Editor editor) {
        ExpandAction expandAction = new ExpandAction(editor);
        Presentation presentation = new Presentation();
        presentation.copyFrom(expandAction.getTemplatePresentation());
        return new ActionButton(expandAction, presentation, ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);
    }





}
