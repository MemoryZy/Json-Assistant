package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.HyperLinks;
import cn.memoryzy.json.ui.basic.HyperLinkJBLabel;
import cn.memoryzy.json.ui.listener.HyperLinkListenerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/7/29
 */
public class SupportDialog extends DialogWrapper {

    private JPanel rootPanel;
    private JBLabel supportHeader;
    private JBLabel supportContent;
    private JBLabel donateHeader;
    private JBLabel donateContent;
    private JLabel wechatLabel;
    private JLabel alipayLabel;
    private ActionLink donateNote;

    private static final String htmlWrapper = "<html><body>{}</body></html>";
    private static final String htmlBoldWrapper = "<html><body><b>{}</b></body></html>";

    public SupportDialog() {
        super((Project) null);
        setModal(false);
        setTitle(JsonAssistantBundle.messageOnSystem("dialog.support.support.header"));
        setOKButtonText(JsonAssistantBundle.messageOnSystem("dialog.support.ok.text"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        initCopyable();
        initLinkListener();

        supportHeader.setIcon(JsonAssistantIcons.DONATE);
        supportHeader.setText(StrUtil.format(htmlBoldWrapper, JsonAssistantBundle.messageOnSystem("dialog.support.support.header")));
        supportContent.setText(StrUtil.format(htmlWrapper, JsonAssistantBundle.messageOnSystem("dialog.support.support.content",
                HyperLinks.GITHUB_LINK, HyperLinks.MARKETPLACE_REVIEWS_LINK, HyperLinks.PLUGIN_SHARE_LINK)));

        donateHeader.setIcon(JsonAssistantIcons.HEART);
        donateHeader.setText(StrUtil.format(htmlBoldWrapper, JsonAssistantBundle.messageOnSystem("dialog.support.donate.header")));
        donateContent.setText(StrUtil.format(htmlWrapper, JsonAssistantBundle.messageOnSystem("dialog.support.donate.content")));

        wechatLabel.setIcon(JsonAssistantIcons.WECHAT_PAY);
        wechatLabel.setText(StrUtil.format(htmlBoldWrapper, JsonAssistantBundle.messageOnSystem("dialog.support.wechat.pay.text")));

        alipayLabel.setIcon(JsonAssistantIcons.ALIPAY);
        alipayLabel.setText(StrUtil.format(htmlBoldWrapper, JsonAssistantBundle.messageOnSystem("dialog.support.alipay.text")));

        donateNote.setIcon(JsonAssistantIcons.LABEL);
        donateNote.setText(JsonAssistantBundle.messageOnSystem("dialog.support.donate.link.text"));

        return rootPanel;
    }


    private void initLinkListener() {
        donateNote.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component source = (Component) e.getSource();
                RelativePoint relativePoint = new RelativePoint(source, new Point(source.getWidth() / 2, source.getHeight() - 25));

                JBPopupFactory.getInstance()
                        .createHtmlTextBalloonBuilder(
                                JsonAssistantBundle.messageOnSystem("dialog.support.donate.note.text",
                                        HyperLinks.PLUGIN_EMAIL_LINK, HyperLinks.EMAIL_LINK),
                                null,
                                JBUI.CurrentTheme.NotificationInfo.backgroundColor(),
                                new HyperLinkListenerImpl())
                        .setShadow(true)
                        .setHideOnAction(true)
                        .setHideOnClickOutside(true)
                        .setHideOnFrameResize(true)
                        .setHideOnKeyOutside(true)
                        .setHideOnLinkClick(true)
                        .setContentInsets(JBUI.insets(10))
                        .createBalloon()
                        .show(relativePoint, Balloon.Position.above);
            }
        });
    }

    private void createUIComponents() {
        supportContent = new HyperLinkJBLabel();
    }

    private void initCopyable() {
        supportHeader.setCopyable(true);
        supportContent.setCopyable(true);
        donateHeader.setCopyable(true);
        donateContent.setCopyable(true);
    }

    @Override
    protected Action @NotNull [] createActions() {
        List<Action> actions = new ArrayList<>();
        actions.add(getOKAction());
        return actions.toArray(new Action[0]);
    }
}
