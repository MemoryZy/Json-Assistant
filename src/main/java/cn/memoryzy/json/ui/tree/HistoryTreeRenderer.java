package cn.memoryzy.json.ui.tree;

import cn.memoryzy.json.service.persistent.state.JsonRecord;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * @author Memory
 * @since 2025/11/11
 */
public class HistoryTreeRenderer extends NodeRenderer {

    private Font chineseFont;
    private Font jbFont;
    private final boolean isFontNotInitialized;

    public HistoryTreeRenderer(int fontSize) {
        this.jbFont = UIUtils.jetBrainsMonoFont(fontSize);
        this.chineseFont = UIUtils.getChineseFont(fontSize);
        this.isFontNotInitialized = null == UIUtils.JETBRAINS_MAPLE_MONO_FONT;
    }

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        HistoryNode historyNode = (HistoryNode) node.getUserObject();

        // 如果是记录节点，且 sourceFile 为空
        if (historyNode.isRecord() && null == ((JsonRecord) historyNode.getValue()).getSourceFile()) {
            setIcon(AllIcons.General.Warning);
        } else {
            setIcon(historyNode.getNodeIcon());
        }

        append(" " + historyNode.getNodeName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

        if (isFontNotInitialized) {
            if (historyNode.isMatched()) {
                setFont(chineseFont);
            } else {
                setFont(jbFont);
            }
        }
    }

    public void assignFontSize(int fontSize) {
        chineseFont =chineseFont.deriveFont((float) fontSize);
        jbFont = jbFont.deriveFont((float) fontSize);
    }

    public Font getChineseFont() {
        return chineseFont;
    }

    public void setChineseFont(Font chineseFont) {
        this.chineseFont = chineseFont;
    }

    public Font getJbFont() {
        return jbFont;
    }

    public void setJbFont(Font jbFont) {
        this.jbFont = jbFont;
    }
}
