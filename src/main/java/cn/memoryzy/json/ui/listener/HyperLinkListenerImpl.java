package cn.memoryzy.json.ui.listener;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;

import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.net.URI;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/29
 */
public class HyperLinkListenerImpl extends HyperlinkAdapter {

    private static final Logger LOG = Logger.getInstance(HyperLinkListenerImpl.class);

    @Override
    protected void hyperlinkActivated(HyperlinkEvent e) {
        String url = e.getDescription();
        if (Objects.equals(UrlType.SHARE.getId(), url)) {
            execShareLinkAction(e, UrlType.SHARE.getUrl());
        } else if (Objects.equals(UrlType.MAIL.getId(), url)) {
            execEmailLinkAction(UrlType.MAIL.getUrl());
        } else {
            BrowserUtil.browse(url);
        }
    }


    private void execEmailLinkAction(String url) {
        try {
            Desktop.getDesktop().mail(new URI(url));
        } catch (Exception ex) {
            LOG.error(ex);
        }
    }

    private void execShareLinkAction(HyperlinkEvent e, String url) {
        PlatformUtil.setClipboard(url);
        Component component = e.getInputEvent().getComponent();
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(JsonAssistantBundle.messageOnSystem("dialog.support.share"),
                        null,
                        JBUI.CurrentTheme.NotificationInfo.backgroundColor(),
                        null)
                .setShadow(true)
                .setHideOnAction(true)
                .setHideOnClickOutside(true)
                .setHideOnFrameResize(true)
                .setHideOnKeyOutside(true)
                .setHideOnLinkClick(true)
                .setFadeoutTime(5000L)
                .createBalloon()
                .show(new RelativePoint(component, new Point(component.getWidth() / 4, component.getHeight())), Balloon.Position.below);
    }

}
