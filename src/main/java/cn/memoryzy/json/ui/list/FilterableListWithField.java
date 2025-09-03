package cn.memoryzy.json.ui.list;

import cn.memoryzy.json.ui.tree.BaseNode;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.LightColors;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.speedSearch.NameFilteringListModel;
import com.intellij.ui.speedSearch.SpeedSearch;
import com.intellij.util.Function;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

/**
 * 自定义可过滤元素的处理类（搜索框与List不分离）
 *
 * @author Memory
 * @since 2025/9/3
 */
public class FilterableListWithField<T extends BaseNode> extends JPanel {

    private final JList<T> myList;
    private final SearchTextField mySearchField;
    private final NameFilteringListModel<T> myModel;
    private final JScrollPane myScrollPane;
    private final MySpeedSearch mySpeedSearch;
    private boolean myAutoPackHeight = true;

    @NotNull
    public static <T> JComponent wrap(@NotNull JList<? extends BaseNode> list, @NotNull JScrollPane scrollPane, @Nullable Function<? super BaseNode, String> namer) {
        return wrap(list, scrollPane, namer, false);
    }

    @NotNull
    public static <T> JComponent wrap(@NotNull JList<? extends BaseNode> list, @NotNull JScrollPane scrollPane, @Nullable Function<? super BaseNode, String> namer,
                                      boolean highlightAllOccurrences) {
        return new FilterableListWithField<>(list, scrollPane, namer, highlightAllOccurrences);
    }

    private FilterableListWithField(@NotNull JList<T> list,
                                    @NotNull JScrollPane scrollPane,
                                    @Nullable Function<? super T, String> namer,
                                    boolean highlightAllOccurrences) {
        super(new BorderLayout());

        myList = list;
        myScrollPane = scrollPane;
        mySearchField = new SearchTextField(false) {
            @Override
            protected boolean preprocessEventForTextField(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN
                        || e.getKeyCode() == KeyEvent.VK_UP) {
                    myList.dispatchEvent(e);
                    return true;
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && getText().isEmpty()) {
                    IdeFocusManager.findInstance().requestFocus(myList, true);
                    return true;
                }
                return false;
            }
        };

        mySearchField.getTextEditor().setFocusable(false);
        mySearchField.setVisible(false);

        add(mySearchField, BorderLayout.NORTH);
        add(myScrollPane, BorderLayout.CENTER);

        mySpeedSearch = new MySpeedSearch(highlightAllOccurrences);
        mySpeedSearch.setEnabled(namer != null);

        myList.addKeyListener(mySpeedSearch);
        int selectedIndex = myList.getSelectedIndex();
        int modelSize = myList.getModel().getSize();
        myModel = new NameFilteringListModel<>(
                myList.getModel(), namer, mySpeedSearch::shouldBeShowing,
                () -> StringUtil.notNullize(mySpeedSearch.getFilter()));
        myList.setModel(myModel);
        if (myModel.getSize() == modelSize) {
            myList.setSelectedIndex(selectedIndex);
        }
        myList.getActionMap().put(TransferHandler.getPasteAction().getValue(Action.NAME), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mySpeedSearch.type(CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor));
                mySpeedSearch.update();
            }
        });

        setBackground(list.getBackground());
        // setFocusable(true);
    }

    @Override
    protected void processFocusEvent(FocusEvent e) {
        super.processFocusEvent(e);
        if (e.getID() == FocusEvent.FOCUS_GAINED) {
            IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(myList, true));
        }
    }

    public boolean resetFilter() {
        boolean hadPattern = mySpeedSearch.isHoldingFilter();
        if (mySearchField.isVisible()) {
            mySpeedSearch.reset();
        }
        return hadPattern;
    }

    public SpeedSearch getSpeedSearch() {
        return mySpeedSearch;
    }

    private final class MySpeedSearch extends SpeedSearch {
        boolean searchFieldShown;
        boolean myInUpdate;

        private MySpeedSearch(boolean highlightAllOccurrences) {
            super(highlightAllOccurrences);
            // native mac "clear button" is not captured by SearchTextField.onFieldCleared
            mySearchField.addDocumentListener(new DocumentAdapter() {
                @Override
                protected void textChanged(@NotNull DocumentEvent e) {
                    if (myInUpdate) return;
                    if (mySearchField.getText().isEmpty()) {
                        mySpeedSearch.reset();
                    }
                }
            });
            installSupplyTo(myList);
        }

        @Override
        public void update() {
            myInUpdate = true;
            mySearchField.getTextEditor().setBackground(UIUtil.getTextFieldBackground());
            onSpeedSearchPatternChanged();
            mySearchField.setText(getFilter());
            if (isHoldingFilter() && !searchFieldShown) {
                mySearchField.setVisible(true);
                searchFieldShown = true;
            } else if (!isHoldingFilter() && searchFieldShown) {
                mySearchField.setVisible(false);
                searchFieldShown = false;
            }

            myInUpdate = false;
            revalidate();
        }

        @Override
        public void noHits() {
            mySearchField.getTextEditor().setBackground(LightColors.RED);
        }

        private void revalidate() {
            JBPopup popup = PopupUtil.getPopupContainerFor(mySearchField);
            if (popup != null) {
                popup.pack(false, myAutoPackHeight);
            }
            FilterableListWithField.this.revalidate();
        }
    }

    private void onSpeedSearchPatternChanged() {
        T prevSelection = myList.getSelectedValue(); // save to restore the selection on filter drop
        myModel.refilter();
        // 不管文本新增还是搜索框清空，都会进入此方法
        int size = myModel.getSize();
        if (size > 0) {
            int fullMatchIndex = mySpeedSearch.isHoldingFilter() ? myModel.getClosestMatchIndex() : myModel.getElementIndex(prevSelection);
            if (fullMatchIndex != -1) {
                myList.setSelectedIndex(fullMatchIndex);
            }

            if (myModel.getSize() <= myList.getSelectedIndex() || !myModel.contains(myList.getSelectedValue())) {
                myList.setSelectedIndex(0);
            }

            // 只要新的 size 小于原 size，表示是匹配成功的节点
            ListModel<T> originalModel = myModel.getOriginalModel();
            int originalSize = originalModel.getSize();
            boolean matched = size < originalSize;

            // 为 匹配成功的 / 所有的节点（根据 matched 变量）都设置匹配成功，并且刷新树
            for (int i = 0; i < size; i++) {
                T element = myModel.getElementAt(i);
                element.setMatched(matched);
            }

            UIUtils.repaintComponent(myList);
        } else {
            mySpeedSearch.noHits();
            revalidate();
        }
    }

    @NotNull
    public JList<T> getList() {
        return myList;
    }

    @NotNull
    public JScrollPane getScrollPane() {
        return myScrollPane;
    }

    public void setAutoPackHeight(boolean autoPackHeight) {
        myAutoPackHeight = autoPackHeight;
    }

    @Override
    public void requestFocus() {
        IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(myList, true));
    }

}
