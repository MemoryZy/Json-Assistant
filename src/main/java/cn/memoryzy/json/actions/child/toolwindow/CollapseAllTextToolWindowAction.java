package cn.memoryzy.json.actions.child.toolwindow;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.codeInsight.folding.impl.FoldingUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.LanguageTextField;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/24
 */
public class CollapseAllTextToolWindowAction extends DumbAwareAction implements UpdateInBackground {

    private final JsonViewerWindow window;

    public CollapseAllTextToolWindowAction(JsonViewerWindow window) {
        super();
        this.window = window;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.collapse.all.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.collapse.all.description"));
        presentation.setIcon(JsonAssistantIcons.InnerAction.COLLAPSE_ALL);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        LanguageTextField jsonTextField = window.getJsonTextField();
        Editor editor = jsonTextField.getEditor();
        int jsonOutsetOffset = JsonUtil.findJsonOutsetCharacterOffset(jsonTextField.getText());
        collapseRegionAtOffset(Objects.requireNonNull(editor), jsonOutsetOffset);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(null != window.getJsonTextField().getEditor() && JsonAssistantUtil.isJsonOrExtract(window.getJsonContent()));
    }

    public static void collapseRegionAtOffset(@NotNull final Editor editor, final int offset) {
        final int line = editor.getDocument().getLineNumber(offset);
        Runnable processor = () -> {
            FoldRegion region = FoldingUtil.findFoldRegionStartingAtLine(editor, line);
            if (region != null && region.isExpanded()){
                region.setExpanded(false);
            } else {
                FoldRegion[] regions = FoldingUtil.getFoldRegionsAtOffset(editor, offset);
                for (FoldRegion region1 : regions) {
                    if (region1.isExpanded()) {
                        region1.setExpanded(false);
                        break;
                    }
                }
            }
        };
        editor.getFoldingModel().runBatchFoldingOperation(processor);
    }

}
