package cn.memoryzy.json.ui.listener;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditorEx;
import org.jetbrains.annotations.NotNull;

/**
 * 修改编辑器光标行颜色、行数超出后重构UI
 *
 * @author Memory
 * @since 2025/6/25
 */
public class EditorLineChangeMonitor implements DocumentListener {
    private static final Logger LOG = Logger.getInstance(EditorLineChangeMonitor.class);

    private final EditorEx editor;
    private int lastLineCount = 0;

    public EditorLineChangeMonitor(EditorEx editor) {
        this.editor = editor;
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        try {
            // -------------- 开启/关闭光标行
            EditorSettings settings = editor.getSettings();
            // 编辑器原来为空，新增不为空，表示新增
            if (StrUtil.isBlank(event.getOldFragment()) && StrUtil.isNotBlank(event.getNewFragment())) {
                if (!settings.isCaretRowShown()) {
                    settings.setCaretRowShown(true);
                }
            } else if (StrUtil.isNotBlank(event.getOldFragment()) && StrUtil.isBlank(event.getNewFragment())) {
                // 编辑器原来不为空，新增为空，表示全部删除
                if (settings.isCaretRowShown()) {
                    settings.setCaretRowShown(false);
                }
            }

            // -------------- 重新绘制
            DocumentEx document = editor.getDocument();
            int newLineCount = document.getLineCount();
            if (lastLineCount != newLineCount) {
                lastLineCount = newLineCount;
                UIUtils.repaintEditor(editor);
            }

        } catch (Error error) {
            LOG.warn("[Json Assistant] " + error);
        }
    }

}
