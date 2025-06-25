package cn.memoryzy.json.ui.panel;

import cn.memoryzy.json.model.EditorContext;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.JsonGridComponentProvider;
import cn.memoryzy.json.ui.JsonQueryComponentProvider;
import cn.memoryzy.json.ui.JsonStructureComponentProvider;
import cn.memoryzy.json.util.UIUtils;
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
    private JsonGridComponentProvider gridProvider;
    private CombineCardLayout cardLayout;

    public JsonAssistantToolWindowPanel(LayoutManager layout) {
        super(layout);
    }

    /**
     * 切换卡片
     */
    public void switchToCard(JsonWrapper wrapper, EditorContext editorContext, String cardName) {
        cardLayout.toggleCard(cardName);
        if (Objects.equals(UIUtils.JSON_TREE_CARD_NAME, cardName)) {
            // 重新生成根节点
            treeProvider.rebuildTree(wrapper, 3, editorContext);

        } else if (Objects.equals(UIUtils.JSON_QUERY_CARD_NAME, cardName)) {
            queryProvider.setDocumentText(editor.getDocument().getText());

        } else if (Objects.equals(UIUtils.JSON_GRID_CARD_NAME, cardName)) {
            gridProvider.rebuildTable(wrapper);
        }
    }

    public static boolean isEditorCardDisplayed(SimpleToolWindowPanel simpleToolWindowPanel) {
        return Optional.ofNullable((JsonAssistantToolWindowPanel) simpleToolWindowPanel.getContent())
                .map(JsonAssistantToolWindowPanel::getCardLayout)
                .map(CombineCardLayout::isEditorCardDisplayed)
                .orElse(false);
    }

    // region Getter、Setter
    public JsonAssistantToolWindowPanel setEditor(EditorEx editor) {
        this.editor = editor;
        return this;
    }

    public JsonAssistantToolWindowPanel setTreeProvider(JsonStructureComponentProvider treeProvider) {
        this.treeProvider = treeProvider;
        return this;
    }

    public JsonAssistantToolWindowPanel setQueryProvider(JsonQueryComponentProvider queryProvider) {
        this.queryProvider = queryProvider;
        return this;
    }

    public JsonAssistantToolWindowPanel setGridProvider(JsonGridComponentProvider gridProvider) {
        this.gridProvider = gridProvider;
        return this;
    }

    public JsonAssistantToolWindowPanel setCardLayout(CombineCardLayout cardLayout) {
        this.cardLayout = cardLayout;
        return this;
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

    public JsonGridComponentProvider getGridProvider() {
        return gridProvider;
    }

    public CombineCardLayout getCardLayout() {
        return cardLayout;
    }

    // endregion
}
