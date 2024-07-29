package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.HyperLinks;
import cn.memoryzy.json.listener.HyperLinkListenerImpl;
import cn.memoryzy.json.ui.basic.HyperLinkJBLabel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBLabel;
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
                JBColor color = new JBColor(new Color(186, 238, 186), new Color(51, 65, 46));

                JBPopupFactory.getInstance()
                        .createHtmlTextBalloonBuilder("<p>使用微信赞赏 / 支付宝捐赠时请留言提供您的 <b>名称（昵称）</b> 和 <b>网站</b>。</p>\n" +
                                "<p>赞助者名称及网站将被添加至赞助者列表中。</p>\n" +
                                "<p>有疑问的话，可通过 <a href=\"" + HyperLinks.PLUGIN_EMAIL_LINK + "\">邮箱</a> 与我联系。</p>", JsonAssistantIcons.HEART, color, new HyperLinkListenerImpl())

                        .setShadow(true)
                        .setHideOnAction(true)
                        .setHideOnClickOutside(true)
                        .setHideOnFrameResize(true)
                        .setHideOnKeyOutside(true)
                        .setHideOnLinkClick(true)
                        .createBalloon()
                        .show(relativePoint, Balloon.Position.above);
            }
        });
    }

    /**
     * 计算组件下方的位置
     *
     * @param component 组件
     * @return 位置
     */
    public static RelativePoint calculateBelowPoint(Component component) {
        int offsetX = 0;
        int offsetY = 1;
        // 获取组件大小
        Dimension size = component.getSize();
        Point point;

        if (size != null) {
            int x = size.width / 2;
            int y = size.height;

            point = new Point(x + offsetX, y + offsetY);
        } else {
            // 如果 size 为 null，则使用 offsetX 和 offsetY 作为默认坐标
            point = new Point(offsetX, offsetY);
        }

        return new RelativePoint(component, point);
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
