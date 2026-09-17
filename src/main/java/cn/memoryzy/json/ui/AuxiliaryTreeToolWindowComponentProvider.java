package cn.memoryzy.json.ui;

import cn.memoryzy.json.model.EditorContext;
import cn.memoryzy.json.model.structure.StructureSetting;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.panel.AuxiliaryTreeToolWindowPanel;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/12/12
 */
public class AuxiliaryTreeToolWindowComponentProvider {

    private final JsonStructureComponentProvider provider;

    /**
     * 构造器
     *
     * @param wrapper JSON对象
     */
    public AuxiliaryTreeToolWindowComponentProvider(JsonWrapper wrapper, @NotNull JComponent component, EditorContext editorContext) {
        provider = new JsonStructureComponentProvider(wrapper, component, getStructureSetting(editorContext));
    }

    public JComponent createComponent() {
        // 获取树
        JPanel treeComponent = provider.getTreeComponent();
        Tree tree = provider.getTree();

        // 在工具窗口中，可能字体需略微调大一点
        Font font = tree.getFont();
        tree.setFont(font.deriveFont((float) (font.getSize() + 1)));

        AuxiliaryTreeToolWindowPanel panel = new AuxiliaryTreeToolWindowPanel(new BorderLayout());
        panel.setTree(tree);
        panel.setTreeComponent(treeComponent);
        panel.add(treeComponent, BorderLayout.CENTER);

        SimpleToolWindowPanel simpleToolWindowPanel = new SimpleToolWindowPanel(false, false);
        simpleToolWindowPanel.setContent(panel);
        return simpleToolWindowPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return provider.getTree();
    }

    private StructureSetting getStructureSetting(EditorContext editorContext) {
        return new StructureSetting().setNeedBorder(false).setNeedToolbar(true).setExpandLevel(3).setEditorContext(editorContext);
    }

}
