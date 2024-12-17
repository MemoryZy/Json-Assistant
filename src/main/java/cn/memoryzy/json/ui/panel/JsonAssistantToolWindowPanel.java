package cn.memoryzy.json.ui.panel;

import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.JsonStructureComponentProvider;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class JsonAssistantToolWindowPanel extends JPanel {

    private EditorEx editor;
    private JsonStructureComponentProvider treeProvider;
    private EditorTreeCardLayout cardLayout;

    public JsonAssistantToolWindowPanel(LayoutManager layout) {
        super(layout);
    }

    /**
     * 切换卡片
     */
    public void switchToCard(JsonWrapper wrapper, boolean switchToEditor) {
        cardLayout.toggleCard();
        // 如果是切换回编辑器，则不需要生成树节点
        if (!switchToEditor) {
            // 重新生成根节点
            treeProvider.rebuildTree(wrapper);
        }
    }

    public static boolean isEditorCardDisplayed(SimpleToolWindowPanel simpleToolWindowPanel) {
        return Optional.ofNullable((JsonAssistantToolWindowPanel) simpleToolWindowPanel.getContent())
                .map(JsonAssistantToolWindowPanel::getCardLayout)
                .map(EditorTreeCardLayout::isEditorCardDisplayed)
                .orElse(false);
    }

    public void setEditor(EditorEx editor) {
        this.editor = editor;
    }

    public void setTreeProvider(JsonStructureComponentProvider treeProvider) {
        this.treeProvider = treeProvider;
    }

    @NotNull
    public EditorEx getEditor() {
        return editor;
    }

    public JsonStructureComponentProvider getTreeProvider() {
        return treeProvider;
    }

    public EditorTreeCardLayout getCardLayout() {
        return cardLayout;
    }

    public void setCardLayout(EditorTreeCardLayout cardLayout) {
        this.cardLayout = cardLayout;
    }
}
