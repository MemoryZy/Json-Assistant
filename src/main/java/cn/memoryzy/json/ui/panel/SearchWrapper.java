package cn.memoryzy.json.ui.panel;

import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.UIUtil;

import java.awt.*;

/**
 * @author Memory
 * @since 2024/12/17
 */
public class SearchWrapper extends NonOpaquePanel {

    public SearchWrapper() {
        super(new BorderLayout());
        initComponents();
    }

    private void initComponents() {

    }

    @Override
    public void updateUI() {
        super.updateUI();
        this.setBackground(UIUtil.getTextFieldBackground());
    }
}