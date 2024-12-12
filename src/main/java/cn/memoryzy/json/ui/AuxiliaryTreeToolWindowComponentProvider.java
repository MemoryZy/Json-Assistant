package cn.memoryzy.json.ui;

import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.component.AuxiliaryTreeToolWindowPanel;
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

    private final JsonWrapper wrapper;
    private final JComponent component;

    /**
     * 构造器
     *
     * @param wrapper   JSON对象
     * @param component 用于注册操作快捷键的组件
     */
    public AuxiliaryTreeToolWindowComponentProvider(JsonWrapper wrapper, @NotNull JComponent component) {
        this.wrapper = wrapper;
        this.component = component;
    }

    public JComponent createComponent() {
        // 创建树结构
        JsonStructureComponentProvider provider = new JsonStructureComponentProvider(wrapper, component);
        JPanel treeComponent = provider.getTreeComponent();
        Tree tree = provider.getTree();

        // 在工具窗口中，可能字体需略微调大一点
        Font font = tree.getFont();
        tree.setFont(font.deriveFont((float) (font.getSize() + 1)));

        AuxiliaryTreeToolWindowPanel panel = new AuxiliaryTreeToolWindowPanel(new BorderLayout(), tree, treeComponent);
        panel.add(treeComponent, BorderLayout.CENTER);

        SimpleToolWindowPanel simpleToolWindowPanel = new SimpleToolWindowPanel(false, false);
        simpleToolWindowPanel.setContent(panel);
        return simpleToolWindowPanel;
    }

}
