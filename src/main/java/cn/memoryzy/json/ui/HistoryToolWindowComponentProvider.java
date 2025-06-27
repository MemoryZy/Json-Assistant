package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.service.persistent.state.v2.JsonRecord;
import cn.memoryzy.json.service.persistent.v2.HistoryManager;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * @author Memory
 * @since 2025/6/26
 */
public class HistoryToolWindowComponentProvider implements Disposable {

    private final Project project;
    private final HistoryManager historyManager;

    private final Editor recordEditor;

    public HistoryToolWindowComponentProvider(Project project) {
        this.project = project;
        this.historyManager = HistoryManager.getInstance(project);

        this.recordEditor = createJsonEditor("record", true, EditorKind.MAIN_EDITOR);
    }

    public JComponent createComponent() {
        JBSplitter splitter = new JBSplitter(true, 0.4f);
        splitter.setFirstComponent(createFirstComponent());
        splitter.setSecondComponent(createSecondComponent());

        SimpleToolWindowPanel windowPanel = new SimpleToolWindowPanel(true, false);
        // windowPanel.setToolbar(createToolbar(windowPanel));
        windowPanel.setContent(splitter);
        return windowPanel;
    }

    private JComponent createFirstComponent() {
        // TODO 也可以在jblist 和 tree 上方加一个输入框，负责搜索，搜到了，其他结果隐藏起来

        // CardLayout 切换 Tree 和 JBList 展示
        JBList<JsonRecord> showList = new JBList<>(fillHistoryListModel());
        showList.setFont(UIUtils.jetBrainsMonoFont(13));
        showList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showList.setCellRenderer(new StyleListCellRenderer());
        showList.setEmptyText(JsonAssistantBundle.messageOnSystem("dialog.history.empty.text"));


        // TODO 当点击修改按钮时，把列表隐藏，展示一个输入框、一个编辑器，在其中编辑名称及json，还有一个按钮

        return new BorderLayoutPanel().addToCenter(showList);
    }

    private JComponent createSecondComponent() {
        // 一个标签、一个编辑器面板
        JBLabel recordLabel = new JBLabel(JsonAssistantBundle.messageOnSystem("json.history.record"));
        return new BorderLayoutPanel().addToTop(recordLabel).addToCenter(recordEditor.getComponent());
    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(recordEditor);
    }

    private DefaultListModel<JsonRecord> fillHistoryListModel() {
        List<JsonRecord> recentHistories = historyManager.getRecentHistories();
        return JBList.createDefaultListModel(recentHistories);
    }


    private Editor createJsonEditor(String fileName, boolean isViewer, EditorKind kind) {
        // TODO 后续这里可以改为跟随主编辑器的设置
        Editor editor = PlatformUtil.createEditor(project, fileName, FileTypeHolder.JSON5, isViewer, kind, "");
        editor.getSettings().setLineNumbersShown(false);
        // 标记编辑器
        // editor.putUserData(QUERY_EDITOR_FLAG, true);
        return editor;
    }

    static class StyleListCellRenderer extends ColoredListCellRenderer<JsonRecord> {
        @Override
        protected void customizeCellRenderer(@NotNull JList<? extends JsonRecord> list, JsonRecord value, int index, boolean selected, boolean hasFocus) {
            String name = value.getName();
            append((index + 1) + "  ", SimpleTextAttributes.GRAY_ATTRIBUTES, false);
            append(" " + (StrUtil.isNotBlank(name) ? name : value.getDisplayText()), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
            setIcon(AllIcons.FileTypes.Json);
            SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, selected);
        }
    }

}
