package cn.memoryzy.json.ui.panel;

import cn.memoryzy.json.action.query.ShowHistoryAction;
import cn.memoryzy.json.ui.component.SearchHistoryButton;
import cn.memoryzy.json.ui.editor.SearchTextField2;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.function.Predicate;

/**
 * @author Memory
 * @since 2024/12/17
 */
public class SearchWrapper extends NonOpaquePanel {

    public SearchWrapper(Project project, FileType fileType, Predicate<String> action) {
        super(new BorderLayout());
        initComponents(project, fileType, action);
    }

    private void initComponents(Project project, FileType fileType, Predicate<String> action) {
        SearchTextField2 searchTextField2 = new SearchTextField2(project, fileType, action);
        ShowHistoryAction showHistoryAction = new ShowHistoryAction(this, searchTextField2);
        SearchHistoryButton searchHistoryButton = new SearchHistoryButton(showHistoryAction, false);

        JPanel historyButtonWrapper = new NonOpaquePanel(new BorderLayout());
        historyButtonWrapper.setBorder(JBUI.Borders.empty(3, 6));
        historyButtonWrapper.add(searchHistoryButton, BorderLayout.NORTH);

        add(historyButtonWrapper, BorderLayout.WEST);
        add(searchTextField2, BorderLayout.CENTER);
        setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0));
        setOpaque(true);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        this.setBackground(UIUtil.getTextFieldBackground());
    }
}