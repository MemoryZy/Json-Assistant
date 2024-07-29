package cn.memoryzy.json.listener;

import cn.hutool.core.net.url.UrlBuilder;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.HyperLinks;
import cn.memoryzy.json.ui.JsonToJavaBeanDialog;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.awt.RelativePoint;

import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/29
 */
public class HyperLinkListenerImpl extends HyperlinkAdapter {

    private static final Logger LOG = Logger.getInstance(HyperLinkListenerImpl.class);

    @Override
    protected void hyperlinkActivated(HyperlinkEvent e) {
        String description = e.getDescription();
        if (Objects.equals(HyperLinks.PLUGIN_SHARE_LINK, description)) {
            execShareLinkAction(e);
        } else if (Objects.equals(HyperLinks.PLUGIN_EMAIL_LINK, description)){
            execEmailLinkAction(e);
        } else {
            BrowserUtil.browse(description);
        }
    }

    private void execEmailLinkAction(HyperlinkEvent e) {
        String uri = "mailto:" + HyperLinks.EMAIL_LINK;
        try {
            Desktop.getDesktop().mail(new URI(uri));
        } catch (Exception ex) {
            LOG.error(ex);
        }
    }

    private void execShareLinkAction(HyperlinkEvent e) {
        PlatformUtil.setClipboard(HyperLinks.MARKETPLACE_LINK);
        Component component = e.getInputEvent().getComponent();
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(JsonAssistantBundle.messageOnSystem("dialog.support.share.text"), MessageType.INFO, null)
                .setShadow(true)
                .setFadeoutTime(5000L)
                .createBalloon()
                .show(new RelativePoint(component, new Point(component.getWidth() / 4, component.getHeight())), Balloon.Position.below);
    }

}
