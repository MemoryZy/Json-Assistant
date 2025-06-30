package cn.memoryzy.json.ui.panel;

import cn.memoryzy.json.action.query.ShowHistoryAction;
import cn.memoryzy.json.ui.component.SearchHistoryButton;
import cn.memoryzy.json.ui.editor.SearchTextField2;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Memory
 * @since 2024/12/17
 */
public class SearchWrapper extends TransparentContainer {

    private final SearchTextField2 searchTextField2;

    public SearchWrapper(Project project, FileType fileType, Predicate<String> predicate, Supplier<String> propertyNameSupplier) {
        super(new BorderLayout());
        this.searchTextField2 = new SearchTextField2(project, fileType, predicate, propertyNameSupplier);
        this.initComponents(propertyNameSupplier);
    }

    private void initComponents(Supplier<String> propertyNameSupplier) {
        ShowHistoryAction showHistoryAction = new ShowHistoryAction(this, searchTextField2, propertyNameSupplier);
        SearchHistoryButton searchHistoryButton = new SearchHistoryButton(showHistoryAction, false);

        JPanel historyButtonWrapper = new NonOpaquePanel(new BorderLayout());
        historyButtonWrapper.setBorder(JBUI.Borders.empty(3, 6));
        historyButtonWrapper.add(searchHistoryButton, BorderLayout.NORTH);

        add(historyButtonWrapper, BorderLayout.WEST);
        add(searchTextField2, BorderLayout.CENTER);
        setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0));
    }

    public void clearSearchText() {
        searchTextField2.setText("");
    }

}