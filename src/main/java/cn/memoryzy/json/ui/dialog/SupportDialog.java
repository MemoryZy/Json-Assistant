package cn.memoryzy.json.ui.dialog;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.HtmlConstant;
import cn.memoryzy.json.constant.Urls;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.ui.component.HyperLinkJBLabel;
import cn.memoryzy.json.ui.listener.HyperLinkListenerImpl;
import com.intellij.openapi.application.ApplicationManager;
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

    public SupportDialog() {
        super((Project) null);
        setModal(false);
        setTitle(JsonAssistantBundle.messageOnSystem("dialog.support.support.header"));
        setOKButtonText(JsonAssistantBundle.messageOnSystem("dialog.support.ok"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        initCopyable();
        initLinkListener();

        supportHeader.setIcon(JsonAssistantIcons.DONATE);
        supportHeader.setText(HtmlConstant.wrapBoldHtml(JsonAssistantBundle.messageOnSystem("dialog.support.support.header")));
        supportContent.setText(HtmlConstant.wrapHtml(JsonAssistantBundle.messageOnSystem("dialog.support.support.content",
                Urls.GITHUB_LINK,
                Urls.MARKETPLACE_REVIEWS_LINK,
                UrlType.SHARE.getId())));

        donateHeader.setIcon(JsonAssistantIcons.HEART);
        donateHeader.setText(HtmlConstant.wrapBoldHtml(JsonAssistantBundle.messageOnSystem("dialog.support.donate.header")));
        donateContent.setText(HtmlConstant.wrapHtml(JsonAssistantBundle.messageOnSystem("dialog.support.donate.content")));

        wechatLabel.setIcon(JsonAssistantIcons.WECHAT_PAY);
        wechatLabel.setText(HtmlConstant.wrapBoldHtml(JsonAssistantBundle.messageOnSystem("dialog.support.wechat")));

        alipayLabel.setIcon(JsonAssistantIcons.ALIPAY);
        alipayLabel.setText(HtmlConstant.wrapBoldHtml(JsonAssistantBundle.messageOnSystem("dialog.support.alipay")));

        donateNote.setIcon(JsonAssistantIcons.LABEL);
        donateNote.setText(JsonAssistantBundle.messageOnSystem("dialog.support.donate.link"));

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
                                JsonAssistantBundle.messageOnSystem("dialog.support.donate.note",
                                        Urls.DONORS_LIST_LINK, UrlType.MAIL.getId(), Urls.EMAIL_LINK),
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

    @Override
    public void show() {
        ApplicationManager.getApplication().invokeLater(super::show);
    }
}
