package cn.memoryzy.json.ui.basic;

import com.intellij.ui.LanguageTextField;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class JsonViewerPanel extends JPanel {

    private final LanguageTextField jsonTextField;

    public JsonViewerPanel(LayoutManager layout, LanguageTextField jsonTextField) {
        super(layout);
        this.jsonTextField = jsonTextField;
    }

    public LanguageTextField getJsonTextField() {
        return jsonTextField;
    }
}
