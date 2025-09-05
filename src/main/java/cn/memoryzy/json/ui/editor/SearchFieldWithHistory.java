package cn.memoryzy.json.ui.editor;

import cn.hutool.core.util.StrUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Memory
 * @since 2025/9/5
 */
public class SearchFieldWithHistory extends ExtendableTextField {

    private final Project project;

    /**
     * 确认后要执行的操作
     */
    private final Predicate<String> predicate;

    /**
     * 历史记录持久化 Key 的提供者
     */
    private final Supplier<String> propertySupplier;


    public SearchFieldWithHistory(Project project, Predicate<String> predicate, Supplier<String> propertySupplier) {
        addExtension(new SearchExtension());
        this.project = project;
        this.predicate = predicate;
        this.propertySupplier = propertySupplier;

        DumbAwareAction.create(event -> {
            showPopup();
        }).registerCustomShortcutSet(KeymapUtil.getActiveKeymapShortcuts("ShowSearchHistory"), this);
    }

    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && pressed) {
            String text = getText();
            if (predicate.test(text)) {
                // 添加历史记录
                addHistory(text);
            }

            return true;
        }

        return super.processKeyBinding(ks, e, condition, pressed);
    }

    public SearchFieldWithHistory configureBorderless(Color backgroud) {
        if (null != backgroud) setBackground(backgroud);

        setOpaque(false);
        setBorder(JBUI.Borders.empty());
        return this;
    }

    private void showPopup() {
        JBPopupFactory.getInstance()
                .createPopupChooserBuilder(getHistory())
                .setMovable(false)
                .setResizable(false)
                .setRequestFocus(true)
                .setItemChosenCallback(item -> {
                    setText(item);
                    IdeFocusManager.getGlobalInstance().requestFocus(this, false);
                })
                .createPopup()
                .showUnderneathOf(this);
    }

    private void addHistory(String text) {
        if (StrUtil.isBlank(text)) return;

        String propertyName = propertySupplier.get();
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
        PropertiesComponent.getInstance(project).setValue(propertySupplier.get(), StrUtil.join("\n", histories));
    }

    public List<String> getHistory() {
        String history = PropertiesComponent.getInstance(project).getValue(propertySupplier.get());
        return StrUtil.isNotBlank(history) ? StrUtil.split(history, '\n') : List.of();
    }

    private final class SearchExtension implements Extension {

        @Override
        public Icon getIcon(boolean hovered) {
            return AllIcons.Actions.SearchWithHistory;
        }

        @Override
        public int getAfterIconOffset() {
            return JBUIScale.scale(6);
        }

        @Override
        public int getIconGap() {
            return JBUIScale.scale(2);
        }

        @Override
        public boolean isIconBeforeText() {
            return true;
        }

        @Override
        public String getTooltip() {
            return IdeBundle.message("tooltip.search.history")
                    + " (" + KeymapUtil.getFirstKeyboardShortcutText("ShowSearchHistory") + ")";
        }

        @Override
        public Runnable getActionOnClick() {
            return SearchFieldWithHistory.this::showPopup;
        }

        @Override
        public String toString() {
            return "search";
        }
    }

}
