package cn.memoryzy.json.ui.list;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.LightColors;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.speedSearch.NameFilteringListModel;
import com.intellij.ui.speedSearch.SpeedSearch;
import com.intellij.util.Function;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * 自定义可过滤元素的处理类
 *
 * @author Memory
 * @since 2025/8/20
 */
public class FilterableList<T> {

    private final JList<T> list;
    private final SearchTextField filterField;
    private final NameFilteringListModel<T> model;
    private final ListFilterSpeedSearch speedSearch;

    public FilterableList(@NotNull JList<T> list, @Nullable Function<? super T, String> nameFunction, boolean highlightAllOccurrences) {
        // 想要实现被搜索的元素高亮的话，需要在外部 setCellRenderer(new ColoredListCellRenderer<>())
        // 并且使用 SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, selected) 来实现
        this.list = list;
        this.filterField = new SearchTextField(false) {
            @Override
            protected boolean preprocessEventForTextField(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN
                        || e.getKeyCode() == KeyEvent.VK_UP) {
                    list.dispatchEvent(e);
                    return true;
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && getText().isEmpty()) {
                    IdeFocusManager.findInstance().requestFocus(list, true);
                    return true;
                }
                return false;
            }
        };

        speedSearch = new ListFilterSpeedSearch(highlightAllOccurrences);
        speedSearch.setEnabled(nameFunction != null);

        this.list.addKeyListener(speedSearch);
        int selectedIndex = this.list.getSelectedIndex();
        int modelSize = this.list.getModel().getSize();
        model = new NameFilteringListModel<>(
                this.list.getModel(), nameFunction, speedSearch::shouldBeShowing,
                () -> StringUtil.notNullize(speedSearch.getFilter()));
        this.list.setModel(model);
        if (model.getSize() == modelSize) {
            this.list.setSelectedIndex(selectedIndex);
        }

        this.list.getActionMap().put(TransferHandler.getPasteAction().getValue(Action.NAME), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                speedSearch.type(CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor));
                speedSearch.update();
            }
        });
    }

    public boolean resetFilter() {
        boolean hadPattern = speedSearch.isHoldingFilter();
        speedSearch.reset();
        return hadPattern;
    }

    public void replaceAll(List<T> dataList) {
        model.replaceAll(dataList);
        list.revalidate();
        list.repaint();
    }

    @NotNull
    public ListModel<T> getOriginalModel() {
        return model.getOriginalModel();
    }

    private final class ListFilterSpeedSearch extends SpeedSearch {
        boolean myInUpdate = false;

        private ListFilterSpeedSearch(boolean highlightAllOccurrences) {
            super(highlightAllOccurrences);
            // native mac "clear button" is not captured by SearchTextField.onFieldCleared
            filterField.addDocumentListener(new DocumentAdapter() {
                @Override
                protected void textChanged(@NotNull DocumentEvent e) {
                    if (!myInUpdate) {
                        myInUpdate = true;
                        try {
                            String text = filterField.getText();
                            updatePattern(text);
                            update();
                        } finally {
                            myInUpdate = false;
                        }
                    }

                    if (filterField.getText().isEmpty()) {
                        speedSearch.reset();
                    }
                }
            });
            setEnabled(true);
            installSupplyTo(list);
        }

        @Override
        public void update() {
            myInUpdate = true;
            // 恢复原本的背景色
            filterField.getTextEditor().setBackground(UIUtil.getTextFieldBackground());
            onSpeedSearchPatternChanged();
            filterField.setText(getFilter());
            myInUpdate = false;
        }

        @Override
        public void noHits() {
            filterField.getTextEditor().setBackground(LightColors.RED);
        }
    }

    private void onSpeedSearchPatternChanged() {
        T prevSelection = list.getSelectedValue(); // save to restore the selection on filter drop
        model.refilter();
        if (model.getSize() > 0) {
            int fullMatchIndex = speedSearch.isHoldingFilter() ? model.getClosestMatchIndex() : model.getElementIndex(prevSelection);
            if (fullMatchIndex != -1) {
                list.setSelectedIndex(fullMatchIndex);
            }

            if (model.getSize() <= list.getSelectedIndex() || !model.contains(list.getSelectedValue())) {
                list.setSelectedIndex(0);
            }
        } else {
            // 如果没有匹配到值，就显示红背景色
            speedSearch.noHits();
        }
    }

    public SearchTextField configureBorderlessFilterField() {
        filterField.setOpaque(false);
        filterField.setBorder(JBUI.Borders.empty());

        JBTextField editor = filterField.getTextEditor();
        editor.setOpaque(false);
        editor.setBorder(JBUI.Borders.empty());
        return filterField;
    }

    public JList<T> getList() {
        return list;
    }

    public SearchTextField getFilterField() {
        return filterField;
    }
}
