package cn.memoryzy.json.ui.editor;

import cn.hutool.core.util.StrUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Memory
 * @since 2025/6/27
 */
public class ModernTextFieldWithAutoCompletion extends TextFieldWithAutoCompletion<String> {

    private final Project project;
    private final Supplier<String> propertyNameSupplier;

    public ModernTextFieldWithAutoCompletion(@Nullable Project project, Collection<String> variants, Supplier<String> propertyNameSupplier) {
        super(project, new StringsCompletionProvider(variants, null), false, null);
        this.project = project;
        this.propertyNameSupplier = propertyNameSupplier;
    }

    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && pressed) {
            String text = getText();
            // 添加历史记录
            addHistory(text);
            return true;
        }

        return super.processKeyBinding(ks, e, condition, pressed);
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        editor.setBorder(JBUI.Borders.empty());
        JComponent component = editor.getComponent();
        component.setBorder(JBUI.Borders.empty(5, 0, 3, 6));
        component.setOpaque(false);
        editor.setBackgroundColor(UIUtil.getTextFieldBackground());
        return editor;
    }


    private void addHistory(String text) {
        if (StrUtil.isBlank(text)) return;

        String propertyName = propertyNameSupplier.get();
        String historyStr = PropertiesComponent.getInstance(project).getValue(propertyName);
        List<String> oriHistories = StrUtil.isNotBlank(historyStr) ? StrUtil.split(historyStr, '\n') : List.of();

        ArrayDeque<String> histories = new ArrayDeque<>(oriHistories);
        // 不存在此记录，则添加
        if (!histories.contains(text)) {
            histories.addFirst(text);
            if (histories.size() > 10) {
                histories.removeLast();
            }
            setHistory(histories);
        } else {
            // 存在此记录，移至最前
            if (histories.getFirst().equals(text)) {
                return;
            }
            histories.remove(text);
            histories.addFirst(text);
            setHistory(histories);
        }
    }

    public void setHistory(Collection<String> histories) {
        PropertiesComponent.getInstance(project).setValue(propertyNameSupplier.get(), StrUtil.join("\n", histories));
    }
}
