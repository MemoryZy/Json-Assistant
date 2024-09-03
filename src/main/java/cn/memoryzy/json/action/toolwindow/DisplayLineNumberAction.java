package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.border.Border;

/**
 * @author Memory
 * @since 2024/9/2
 */
public class DisplayLineNumberAction extends ToggleAction implements DumbAware {
    private static final Logger LOG = Logger.getInstance(DisplayLineNumberAction.class);
    public static final String DISPLAY_LINE_NUMBER_ENABLED_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".DisplayLineNumber";

    private final ToolWindowEx toolWindow;

    public DisplayLineNumberAction(ToolWindowEx toolWindow) {
        super();
        this.toolWindow = toolWindow;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.display.line.number.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.display.line.number.description"));

        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String value = propertiesComponent.getValue(DISPLAY_LINE_NUMBER_ENABLED_KEY);
        if (value == null) propertiesComponent.setValue(DISPLAY_LINE_NUMBER_ENABLED_KEY, Boolean.FALSE.toString());
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return isShownLineNumbers();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        Content content = JsonAssistantUtil.getSelectedContent(toolWindow);
        LanguageTextField languageTextField = JsonAssistantUtil.getLanguageTextFieldOnContent(content);
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        if (languageTextField != null) {
            EditorImpl editor = (EditorImpl) languageTextField.getEditor();
            if (editor == null) {
                LOG.error("[Display Line Number] Editor is null");
                propertiesComponent.setValue(DISPLAY_LINE_NUMBER_ENABLED_KEY, Boolean.FALSE.toString());
                return;
            }

            showLineNumber(editor, state);
        }

        if (state) propertiesComponent.setValue(DISPLAY_LINE_NUMBER_ENABLED_KEY, Boolean.TRUE.toString());
        else propertiesComponent.setValue(DISPLAY_LINE_NUMBER_ENABLED_KEY, Boolean.FALSE.toString());
    }

    public static boolean isShownLineNumbers() {
        return Boolean.TRUE.toString().equals(PropertiesComponent.getInstance().getValue(DISPLAY_LINE_NUMBER_ENABLED_KEY));
    }

    public static void showLineNumber(EditorImpl editor, boolean shown) {
        Border border;
        EditorSettings settings = editor.getSettings();
        // 如果需要显示行号，而编辑器正好是展示状态
        boolean shownLineNumbersStatus = settings.isLineNumbersShown();
        if (shown) {
            if (shownLineNumbersStatus) {
                return;
            }

            // 需要显示，而编辑器为非展示状态
            border = JBUI.Borders.emptyTop(2);
        } else {
            if (!shownLineNumbersStatus) {
                return;
            }

            // 不需要显示，而编辑器为显示状态
            border = JBUI.Borders.empty(2, PlatformUtil.isNewUi() ? 0 : 3, 0, 0);
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            settings.setLineNumbersShown(shown);
            editor.setBorder(border);
            editor.reinitSettings();
        });
    }
}
