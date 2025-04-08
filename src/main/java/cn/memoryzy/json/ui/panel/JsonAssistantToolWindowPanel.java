package cn.memoryzy.json.ui.panel;

import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.JsonQueryComponentProvider;
import cn.memoryzy.json.ui.JsonStructureComponentProvider;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class JsonAssistantToolWindowPanel extends JPanel {

    private EditorEx editor;
    private JsonStructureComponentProvider treeProvider;
    private JsonQueryComponentProvider queryProvider;
    private CombineCardLayout cardLayout;

    public JsonAssistantToolWindowPanel(LayoutManager layout) {
        super(layout);
    }

    /**
     * 切换卡片
     */
    public void switchToCard(JsonWrapper wrapper, String cardName) {
        cardLayout.toggleCard(cardName);
        if (Objects.equals(PluginConstant.JSON_TREE_CARD_NAME, cardName)) {
            // 重新生成根节点
            treeProvider.rebuildTree(wrapper, 3);
        } else if (Objects.equals(PluginConstant.JSON_QUERY_CARD_NAME, cardName)) {
            queryProvider.setDocumentText(editor.getDocument().getText());
        }
    }

    public static boolean isEditorCardDisplayed(SimpleToolWindowPanel simpleToolWindowPanel) {
        return Optional.ofNullable((JsonAssistantToolWindowPanel) simpleToolWindowPanel.getContent())
                .map(JsonAssistantToolWindowPanel::getCardLayout)
                .map(CombineCardLayout::isEditorCardDisplayed)
                .orElse(false);
    }

    // region Getter、Setter
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

    public JsonQueryComponentProvider getQueryProvider() {
        return queryProvider;
    }

    public void setQueryProvider(JsonQueryComponentProvider queryProvider) {
        this.queryProvider = queryProvider;
    }

    public CombineCardLayout getCardLayout() {
        return cardLayout;
    }

    public void setCardLayout(CombineCardLayout cardLayout) {
        this.cardLayout = cardLayout;
    }
    // endregion
}
