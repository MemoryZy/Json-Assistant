package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.model.LimitedList;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/9/2
 */
public class LoadLastRecordAction extends ToggleAction implements DumbAware {
    public static final String IMPORT_RECORD_ENABLED_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".ImportLastRecord";

    private final ToolWindowEx toolWindow;

    public LoadLastRecordAction(ToolWindowEx toolWindow) {
        super();
        this.toolWindow = toolWindow;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.editor.toolbar.paste.history.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.editor.toolbar.paste.history.description"));

        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String value = propertiesComponent.getValue(IMPORT_RECORD_ENABLED_KEY);
        if (value == null) propertiesComponent.setValue(IMPORT_RECORD_ENABLED_KEY, Boolean.TRUE.toString());
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return isLoadLastRecord();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        if (state) {
            Project project = getEventProject(e);
            if (project == null) return;
            Content content = JsonAssistantUtil.getSelectedContent(toolWindow);
            EditorEx editor = JsonAssistantUtil.getEditorOnContent(content);
            if (editor != null) {
                DocumentEx document = editor.getDocument();
                if (StrUtil.isBlank(document.getText())) {
                    JsonViewerHistoryState historyState = JsonViewerHistoryState.getInstance(project);
                    LimitedList<String> history = historyState.getHistory();
                    if (CollUtil.isNotEmpty(history)) {
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            document.setText(history.get(0));
                        });
                    }
                }
            }
        }

        PropertiesComponent.getInstance().setValue(IMPORT_RECORD_ENABLED_KEY, Boolean.valueOf(state).toString());
    }

    public static boolean isLoadLastRecord() {
        return Boolean.TRUE.toString().equals(PropertiesComponent.getInstance().getValue(IMPORT_RECORD_ENABLED_KEY));
    }


}
