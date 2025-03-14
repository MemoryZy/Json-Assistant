package cn.memoryzy.json.ui.dialog;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.DataTypeConstant;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.model.BlacklistEntry;
import cn.memoryzy.json.service.persistent.ClipboardDataBlacklistPersistentState;
import cn.memoryzy.json.ui.editor.ViewerModeLanguageTextEditor;
import cn.memoryzy.json.ui.listener.ListRightClickPopupMenuMouseAdapter;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.UIManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.ui.speedSearch.NameFilteringListModel;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2025/3/3
 */
public class JsonBlacklistDialog extends DialogWrapper {

    private final Project project;
    private EditorTextField showTextField;
    private JBList<BlacklistEntry> showList;

    public JsonBlacklistDialog(Project project) {
        super(project, true);
        this.project = project;

        setModal(false);
        setTitle(JsonAssistantBundle.messageOnSystem("dialog.blacklist.title"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        showTextField = new ViewerModeLanguageTextEditor(PlainTextLanguage.INSTANCE, project, "", true);
        showTextField.setFont(UIManager.consolasFont(13));
        showTextField.addNotify();

        showList = new JBList<>(fillBlacklistModel());
        showList.setFont(UIManager.jetBrainsMonoFont(13));
        showList.addListSelectionListener(new UpdateEditorListSelectionListener());
        showList.setEmptyText(JsonAssistantBundle.messageOnSystem("dialog.blacklist.empty.text"));
        showList.setCellRenderer(new StyleListCellRenderer());
        showList.addMouseListener(new ListRightClickPopupMenuMouseAdapter(showList, buildRightMousePopupMenu()));

        selectFirstItemInList();

        UIManager.updateComponentColorsScheme(showList);
        UIManager.updateComponentColorsScheme(showTextField);

        BorderLayoutPanel borderLayoutPanel = new BorderLayoutPanel();
        borderLayoutPanel.addToCenter(UIManager.wrapListWithFilter(showList, BlacklistEntry::getShortText, true));
        borderLayoutPanel.setBorder(JBUI.Borders.empty(3));
        UIManager.rebuildListWithFilter(showList);

        JBSplitter splitter = new JBSplitter(true, 0.3f);
        splitter.setFirstComponent(borderLayoutPanel);
        splitter.setSecondComponent(showTextField);

        ScrollingUtil.installActions(showList);
        ScrollingUtil.ensureSelectionExists(showList);

        splitter.setPreferredSize(JBUI.size(520, 570));

        return splitter;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return showList;
    }

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return UrlType.RECOGNIZE.getId();
    }

    @Override
    public void show() {
        ApplicationManager.getApplication().invokeLater(super::show);
    }

    @Override
    protected Action @NotNull [] createActions() {
        List<Action> actions = new ArrayList<>();
        actions.add(getOKAction());
        actions.add(getHelpAction());
        return actions.toArray(new Action[0]);
    }

    private DefaultListModel<BlacklistEntry> fillBlacklistModel() {
        LinkedList<BlacklistEntry> blacklist = ClipboardDataBlacklistPersistentState.getInstance().blacklist;
        return JBList.createDefaultListModel(blacklist);
    }

    private JPopupMenu buildRightMousePopupMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RemoveAction());

        ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.POPUP, group);
        return actionPopupMenu.getComponent();
    }

    private void selectFirstItemInList() {
        // 选中第一条
        ListModel<BlacklistEntry> listModel = showList.getModel();
        if (listModel.getSize() > 0) {
            showList.setSelectedIndex(0);
        }
    }


    class RemoveAction extends DumbAwareAction {

        public RemoveAction() {
            super(JsonAssistantBundle.message("action.history.remove.text"), JsonAssistantBundle.messageOnSystem("action.history.remove.description"), null);
            registerCustomShortcutSet(CustomShortcutSet.fromString("DELETE"), showList);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            int selectedIndex = showList.getSelectedIndex();
            BlacklistEntry selectedValue = showList.getSelectedValue();

            LinkedList<BlacklistEntry> blacklist = ClipboardDataBlacklistPersistentState.getInstance().blacklist;
            blacklist.removeIf(el -> Objects.equals(el.getId(), selectedValue.getId()));

            // 替换List数据为最新的
            NameFilteringListModel<BlacklistEntry> listModel = (NameFilteringListModel<BlacklistEntry>) showList.getModel();
            listModel.replaceAll(blacklist);

            int size = listModel.getSize();
            if (size == 0) {
                showTextField.setText("");
            } else {
                // 选中被删除元素的前一个元素
                if (selectedIndex > 0) {
                    showList.setSelectedIndex(selectedIndex - 1);
                } else {
                    // 如果还有元素，选中第一个元素
                    showList.setSelectedIndex(0);
                }
            }

            UIManager.rebuildListWithFilter(showList);
        }

        @Override
        public void update(@NotNull AnActionEvent event) {
            event.getPresentation().setEnabled(Objects.nonNull(getEventProject(event)) && Objects.nonNull(showList.getSelectedValue()));
        }
    }

    class UpdateEditorListSelectionListener implements ListSelectionListener {
        private int lastLineCount = 0;

        @Override
        public void valueChanged(ListSelectionEvent e) {
            BlacklistEntry selectedValue = showList.getSelectedValue();
            if (selectedValue != null) {
                showTextField.setText(JsonAssistantUtil.normalizeLineEndings(selectedValue.getOriginalText()));

                // -------------- 重新绘制
                Document document = showTextField.getDocument();
                int newLineCount = document.getLineCount();
                if (lastLineCount != newLineCount) {
                    lastLineCount = newLineCount;
                    UIManager.repaintEditor(Objects.requireNonNull(showTextField.getEditor()));
                }
            }
        }
    }

    static class StyleListCellRenderer extends ColoredListCellRenderer<BlacklistEntry> {
        @Override
        protected void customizeCellRenderer(@NotNull JList<? extends BlacklistEntry> list, BlacklistEntry value, int index, boolean selected, boolean hasFocus) {
            String name = value.getName();
            String originalDataType = value.getOriginalDataType();
            append((index + 1) + "  ", SimpleTextAttributes.GRAY_ATTRIBUTES, false);
            append(" " + (StrUtil.isNotBlank(name) ? name : value.getShortText()), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
            SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, selected);

            Icon icon = AllIcons.FileTypes.Text;
            if (DataTypeConstant.JSON.equals(originalDataType) || DataTypeConstant.JSON5.equals(originalDataType)) {
                icon = AllIcons.FileTypes.Json;
            } else if (DataTypeConstant.YAML.equals(originalDataType)) {
                icon = AllIcons.FileTypes.Yaml;
            } else if (DataTypeConstant.XML.equals(originalDataType)) {
                icon = AllIcons.FileTypes.Xml;
            } else if (DataTypeConstant.URL_PARAM.equals(originalDataType)) {
                icon = JsonAssistantIcons.FileTypes.URL;
            } else if (DataTypeConstant.TOML.equals(originalDataType)) {
                icon = JsonAssistantIcons.FileTypes.TOML;
            }

            setIcon(icon);
        }
    }
}
