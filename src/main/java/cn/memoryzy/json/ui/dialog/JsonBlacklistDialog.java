package cn.memoryzy.json.ui.dialog;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.LanguageHolder;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.model.JsonEntry;
import cn.memoryzy.json.service.persistent.ClipboardDataBlacklistPersistentState;
import cn.memoryzy.json.ui.editor.ViewerModeLanguageTextEditor;
import cn.memoryzy.json.util.UIManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
    private JBList<JsonEntry> showList;

    public JsonBlacklistDialog(Project project) {
        super(project, true);
        this.project = project;

        setModal(false);
        setTitle(JsonAssistantBundle.messageOnSystem("dialog.blacklist.title"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        showTextField = new ViewerModeLanguageTextEditor(LanguageHolder.JSON5, project, "", true);
        showTextField.setFont(UIManager.consolasFont(14));
        showTextField.addNotify();

        // TODO 需要显示格式原文（XML、yaml等），另外构建对象
        showList = new JBList<>(fillBlacklistModel());
        showList.setFont(UIManager.jetBrainsMonoFont(13));
        showList.addListSelectionListener(new JsonHistoryListChooser.UpdateEditorListSelectionListener(showList, showTextField));
        showList.setEmptyText(JsonAssistantBundle.messageOnSystem("dialog.blacklist.empty.text"));

        selectFirstItemInList();

        UIManager.updateComponentColorsScheme(showList);
        UIManager.updateComponentColorsScheme(showTextField);

        new ListSpeedSearch<>(showList);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(showList)
                .setAddAction(new AddAction())
                .setRemoveAction(new RemoveAction())
                .disableUpDownActions();

        JBSplitter splitter = new JBSplitter(true, 0.3f);
        splitter.setFirstComponent(decorator.createPanel());
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
        return UrlType.DEFAULT.getId();
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

    private DefaultListModel<JsonEntry> fillBlacklistModel() {
        LinkedList<JsonEntry> blacklist = ClipboardDataBlacklistPersistentState.getInstance().blacklist;
        return JBList.createDefaultListModel(blacklist);
    }

    private void selectFirstItemInList() {
        // 选中第一条
        ListModel<JsonEntry> listModel = showList.getModel();
        if (listModel.getSize() > 0) {
            showList.setSelectedIndex(0);
        }
    }

    class AddAction implements AnActionButtonRunnable {
        @Override
        public void run(AnActionButton actionButton) {
            new EditorDialog(project).show();
        }
    }

    class RemoveAction implements AnActionButtonRunnable {
        @Override
        public void run(AnActionButton actionButton) {
            int selectedIndex = showList.getSelectedIndex();
            JsonEntry selectedValue = showList.getSelectedValue();

            LinkedList<JsonEntry> blacklist = ClipboardDataBlacklistPersistentState.getInstance().blacklist;
            blacklist.removeIf(el -> Objects.equals(el.getId(), selectedValue.getId()));

            DefaultListModel<JsonEntry> model = (DefaultListModel<JsonEntry>) showList.getModel();
            model.clear();
            model.addAll(blacklist);

            int size = model.getSize();
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
        }
    }
}
