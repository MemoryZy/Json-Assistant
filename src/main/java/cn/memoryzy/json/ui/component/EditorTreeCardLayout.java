package cn.memoryzy.json.ui.component;

import cn.memoryzy.json.constant.PluginConstant;
import com.intellij.ui.JBCardLayout;

import java.awt.*;

/**
 * @author Memory
 * @since 2024/12/13
 */
public class EditorTreeCardLayout extends JBCardLayout {

    private Container parent;
    private String currentCardName;

    public EditorTreeCardLayout() {
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
    public void toggleCard() {
        String name = PluginConstant.JSON_EDITOR_CARD_NAME.equals(currentCardName)
                ? PluginConstant.JSON_TREE_CARD_NAME : PluginConstant.JSON_EDITOR_CARD_NAME;

        show(name);
    }

    public boolean isEditorCardDisplayed() {
        return PluginConstant.JSON_EDITOR_CARD_NAME.equals(currentCardName);
    }

    public boolean isTreeCardDisplayed() {
        return PluginConstant.JSON_TREE_CARD_NAME.equals(currentCardName);
    }

}