package cn.memoryzy.json.ui.panel;

import cn.memoryzy.json.util.UIUtils;
import com.intellij.ui.JBCardLayout;

import java.awt.*;

/**
 * @author Memory
 * @since 2024/12/13
 */
public class CombineCardLayout extends JBCardLayout {

    private Container parent;
    private String currentCardName;

    public CombineCardLayout() {
        super();
    }

    @Override
    public void show(Container parent, String name) {
        super.show(parent, name);
        this.parent = parent;
        this.currentCardName = name;
    }

    public void show(String name) {
        super.show(parent, name);
        this.currentCardName = name;
    }

    /**
     * 切换卡片展示
     */
    public void toggleCard(String cardName) {
        show(cardName);
    }

    public boolean isEditorView() {
        return UIUtils.JSON_EDITOR_CARD_NAME.equals(currentCardName);
    }

    public boolean isTreeView() {
        return UIUtils.JSON_TREE_CARD_NAME.equals(currentCardName);
    }

    public boolean isQueryView() {
        return UIUtils.JSON_QUERY_CARD_NAME.equals(currentCardName);
    }

    public boolean isGridView() {
        return UIUtils.JSON_GRID_CARD_NAME.equals(currentCardName);
    }

}
