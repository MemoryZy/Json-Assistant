package cn.memoryzy.json.ui.panel;

import cn.memoryzy.json.action.query.ShowHistoryAction;
import cn.memoryzy.json.ui.component.EditorButton;
import cn.memoryzy.json.ui.editor.ModernTextFieldWithAutoCompletion;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * @author Memory
 * @since 2025/6/27
 */
public class AutoCompleteWrapper extends TransparentContainer {

    private final ModernTextFieldWithAutoCompletion completion;

    public AutoCompleteWrapper(Project project, Collection<String> variants, Supplier<String> propertyNameSupplier) {
        super(new BorderLayout());
        this.completion = new ModernTextFieldWithAutoCompletion(project, variants);
        this.initComponents(propertyNameSupplier);
    }

    private void initComponents(Supplier<String> propertyNameSupplier) {
        ShowHistoryAction showHistoryAction = new ShowHistoryAction(this, completion, propertyNameSupplier);
        EditorButton searchHistoryButton = new EditorButton(showHistoryAction, false);

        JPanel historyButtonWrapper = new NonOpaquePanel(new BorderLayout());
        historyButtonWrapper.setBorder(JBUI.Borders.empty(3, 6));
        historyButtonWrapper.add(searchHistoryButton, BorderLayout.NORTH);

        add(historyButtonWrapper, BorderLayout.WEST);
        add(completion, BorderLayout.CENTER);
        setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0));
    }

    public JComponent getPreferredFocusedComponent() {
        return completion;
    }

    public void setVariants(Collection<String> variants) {
        completion.setVariants(variants);
    }

    public void globalSchemeChange() {
        completion.globalSchemeChange();
    }

}
