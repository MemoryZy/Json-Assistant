package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.HyperLinks;
import cn.memoryzy.json.constant.LanguageHolder;
import cn.memoryzy.json.model.YamlDocumentModel;
import cn.memoryzy.json.ui.component.editor.ViewerModeLanguageTextEditor;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/9/25
 */
@SuppressWarnings("DuplicatedCode")
public class MultiYamlDocumentChooser extends DialogWrapper {

    private JList<YamlDocumentModel> showList;
    private EditorTextField showTextField;

    private final List<Object> values;

    public MultiYamlDocumentChooser(List<Object> values) {
        super((Project) null, true);
        this.values = values;

        setTitle(JsonAssistantBundle.messageOnSystem("multi.yaml.document.chooser.window.title"));
        setOKButtonText(JsonAssistantBundle.messageOnSystem("multi.yaml.document.chooser.window.ok.button.text"));
        setCancelButtonText(JsonAssistantBundle.messageOnSystem("multi.yaml.document.chooser.window.cancel.button.text"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        showTextField = new ViewerModeLanguageTextEditor(LanguageHolder.YAML, null, "", true);
        showTextField.setFont(JBUI.Fonts.create("Consolas", 14));

        List<YamlDocumentModel> models = YamlDocumentModel.of(values);
        DefaultListModel<YamlDocumentModel> defaultListModel = JBList.createDefaultListModel(models);
        showList = new JBList<>(defaultListModel);
        showList.setFont(JBUI.Fonts.create("JetBrains Mono", 13));
        showList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showList.addListSelectionListener(new SetWholeContentListSelectionListener());
        showList.setCellRenderer(new IconListCellRenderer());

        // 选中第一条
        if (!models.isEmpty()) {
            showList.setSelectedIndex(0);
        }

        JBScrollPane scrollPane = new JBScrollPane(showList) {
            @Override
            public Dimension getPreferredSize() {
                Dimension preferredSize = super.getPreferredSize();
                if (!isPreferredSizeSet()) {
                    setPreferredSize(new Dimension(0, preferredSize.height));
                }
                return preferredSize;
            }
        };

        scrollPane.setBorder(IdeBorderFactory.createBorder(SideBorder.ALL));
        scrollPane.setViewportBorder(JBUI.Borders.empty());

        JBLabel label = new JBLabel(JsonAssistantBundle.messageOnSystem("multi.yaml.document.chooser.window.tip"));
        // label.setFont();
        label.setIcon(AllIcons.Actions.IntentionBulb);
        label.setBorder(JBUI.Borders.emptyBottom(8));
        label.setForeground(JBColor.GRAY);
        label.withFont(JBUI.Fonts.label().asBold());

        JPanel firstPanel = new JPanel(new BorderLayout());
        firstPanel.add(label, BorderLayout.NORTH);
        firstPanel.add(scrollPane, BorderLayout.CENTER);
        firstPanel.setBorder(JBUI.Borders.empty(3));

        JBSplitter splitter = new JBSplitter(true, 0.3f);
        splitter.setFirstComponent(firstPanel);
        splitter.setSecondComponent(showTextField);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(splitter, BorderLayout.CENTER);
        rootPanel.setPreferredSize(new Dimension(450, 500));

        return rootPanel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return showList;
    }

    @Override
    protected Action @NotNull [] createActions() {
        List<Action> actions = new ArrayList<>();
        actions.add(getOKAction());
        actions.add(getCancelAction());
        actions.add(getHelpAction());
        return actions.toArray(new Action[0]);
    }

    @Override
    protected void doHelpAction() {
        BrowserUtil.browse(HyperLinks.OVERVIEW);
    }

    @Override
    protected void doOKAction() {
        if (getOKAction().isEnabled()) {
            // 执行逻辑
            if (executeOkAction()) {
                close(OK_EXIT_CODE);
            }
        }
    }

    private boolean executeOkAction() {
        YamlDocumentModel selectedValue = showList.getSelectedValue();
        return selectedValue != null;
    }

    public YamlDocumentModel getSelectValue() {
        return showList.getSelectedValue();
    }

    public static class IconListCellRenderer extends ColoredListCellRenderer<YamlDocumentModel> {
        @Override
        protected void customizeCellRenderer(@NotNull JList<? extends YamlDocumentModel> list, YamlDocumentModel value,
                                             int index, boolean selected, boolean hasFocus) {
            append((index + 1) + "  ", SimpleTextAttributes.GRAY_ATTRIBUTES);
            append(" " + value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            setIcon(AllIcons.FileTypes.Yaml);
        }
    }

    public class SetWholeContentListSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            YamlDocumentModel selectedValue = showList.getSelectedValue();
            if (selectedValue != null) {
                showTextField.setText(selectedValue.getWholeContent());
            }
        }
    }

}
