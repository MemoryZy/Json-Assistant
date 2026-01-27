package cn.memoryzy.json.ui.panel;

import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.ide.ui.newItemPopup.NewItemWithTemplatesPopupPanel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.BiFunction;

public class CreateWithTemplatesDialogPanel extends NewItemWithTemplatesPopupPanel<CreateWithTemplatesDialogPanel.TemplatePresentation> {

    private boolean templateExplicitlySelected = false;

    public CreateWithTemplatesDialogPanel(@Nullable String selectedItem, @NotNull List<TemplatePresentation> templates) {
        super(templates, new TemplateListCellRenderer());
        myTemplatesList.addListSelectionListener(e -> {
            TemplatePresentation selectedValue = myTemplatesList.getSelectedValue();
            if (selectedValue != null) {
                setTextFieldIcon(selectedValue.icon);
            }
        });
        if (PlatformUtil.isNewUi()) {
            myTemplatesList.setBackground(JBUI.CurrentTheme.Popup.BACKGROUND);
        }
        selectTemplate(selectedItem);
        setTemplatesListVisible(templates.size() > 1);
    }

    public JTextField getNameField() {
        return myTextField;
    }

    public @NotNull String getEnteredName() {
        return myTextField.getText().trim();
    }

    public @NotNull String getSelectedTemplate() {
        return myTemplatesList.getSelectedValue().templateName;
    }

    private void setTextFieldIcon(Icon icon) {
        myTextField.setExtensions(new TemplateIconExtension(icon));
        myTextField.repaint();
    }

    private void selectTemplate(@Nullable String selectedItem) {
        ListModel<TemplatePresentation> model = myTemplatesList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            String templateID = model.getElementAt(i).templateName;
            if (StringUtil.equals(selectedItem, templateID)) {
                myTemplatesList.setSelectedIndex(i);
                return;
            }
        }

        myTemplatesList.setSelectedIndex(0);
    }

    public void setTemplateSelectorMatcher(BiFunction<? super String, ? super TemplatePresentation, Boolean> templateMatcher) {
        selectTemplate(templateMatcher, "");
        myTemplatesList.addListSelectionListener(ListSelectionEvent -> {
            templateExplicitlySelected = true;
        });
        myTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                selectTemplate(templateMatcher, "" + e.getKeyChar());
            }
        });
    }

    private void selectTemplate(BiFunction<? super String, ? super TemplatePresentation, Boolean> templateMatcher, String aChar) {
        if (templateExplicitlySelected) {
            return;
        }
        String newElementName = getEnteredName() + aChar;
        JList<TemplatePresentation> list = myTemplatesList;
        TemplatePresentation matchedElement = null;
        for (int i = 0; i < list.getModel().getSize(); i++) {
            TemplatePresentation presentation = list.getModel().getElementAt(i);
            if (templateMatcher.apply(newElementName, presentation)) {
                matchedElement = presentation;
                break;
            }
        }
        if (matchedElement != null) {
            list.setSelectedValue(matchedElement, true);
            templateExplicitlySelected = false;
        }
    }

    private static final class TemplateListCellRenderer implements ListCellRenderer<TemplatePresentation> {
        private final ListCellRenderer<TemplatePresentation> delegateRenderer =
                SimpleListCellRenderer.create((@NotNull JBLabel label, TemplatePresentation value, int index) -> {
                    if (value != null) {
                        if (null != value.icon) {
                            label.setIcon(value.icon);
                        }
                        label.setText(value.kind);
                    }
                });

        @Override
        public Component getListCellRendererComponent(JList<? extends TemplatePresentation> list,
                                                      TemplatePresentation value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            JComponent delegate = (JComponent) delegateRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            delegate.setBorder(JBUI.Borders.empty(JBUIScale.scale(3),
                    // 新ui的左边需要调大
                    PlatformUtil.isNewUi() ? JBUIScale.scale(20) : JBUIScale.scale(2),
                    JBUIScale.scale(3),
                    JBUIScale.scale(1)));
            return delegate;
        }
    }

    public static class TemplatePresentation {

        private final @NotNull String kind;
        private final @Nullable Icon icon;
        private final @NotNull String templateName;

        public TemplatePresentation(@NotNull String kind, @Nullable Icon icon, @NotNull String templateName) {
            this.kind = kind;
            this.icon = icon;
            this.templateName = templateName;
        }
    }

    private static final class TemplateIconExtension implements ExtendableTextComponent.Extension {
        private final Icon icon;

        private TemplateIconExtension(Icon icon) {
            this.icon = icon;
        }

        @Override
        public Icon getIcon(boolean hovered) {
            return icon;
        }

        @Override
        public boolean isIconBeforeText() {
            return true;
        }
    }

}