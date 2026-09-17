package cn.memoryzy.json.ui.panel;

import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.UIUtil;

import java.awt.*;

/**
 * @author Memory
 * @since 2025/6/30
 */
public class TransparentContainer extends NonOpaquePanel {

    public TransparentContainer() {
        super();
    }

    public TransparentContainer(LayoutManager layout) {
        super(layout);
        setOpaque(true);
        setBackground(UIUtil.getTextFieldBackground());
    }

    @Override
    public void updateUI() {
        super.updateUI();
        this.setBackground(UIUtil.getTextFieldBackground());
    }

}
