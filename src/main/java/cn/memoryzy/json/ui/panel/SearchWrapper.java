package cn.memoryzy.json.ui.panel;

import cn.memoryzy.json.action.query.ShowHistoryAction;
import cn.memoryzy.json.ui.component.SearchHistoryButton;
import cn.memoryzy.json.ui.editor.SearchTextField2;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/12/17
 */
public class SearchWrapper extends NonOpaquePanel {


    public SearchWrapper(Project project, FileType fileType, Runnable action) {
        super(new BorderLayout());
        initComponents(project, fileType, action);
    }

    private void initComponents(Project project, FileType fileType, Runnable action) {
        SearchTextField2 searchTextField2 = new SearchTextField2(project, fileType, action);
        ShowHistoryAction showHistoryAction = new ShowHistoryAction(this, searchTextField2);
        SearchHistoryButton searchHistoryButton = new SearchHistoryButton(showHistoryAction, false);

        JPanel historyButtonWrapper = new NonOpaquePanel(new BorderLayout());
        // historyButtonWrapper.border = JBUI.Borders.empty(3, 6, 3, 6)
        // historyButtonWrapper.add(historyButton, BorderLayout.NORTH)

    }

    @Override
    public void updateUI() {
        super.updateUI();
        this.setBackground(UIUtil.getTextFieldBackground());
    }
}