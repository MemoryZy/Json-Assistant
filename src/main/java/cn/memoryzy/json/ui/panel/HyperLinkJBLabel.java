package cn.memoryzy.json.ui.panel;

import cn.memoryzy.json.ui.listener.HyperLinkListenerImpl;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkListener;

/**
 * @author Memory
 * @since 2024/7/29
 */
public class HyperLinkJBLabel extends JBLabel {
    @Override
    protected @NotNull HyperlinkListener createHyperlinkListener() {
        return new HyperLinkListenerImpl();
    }
}
