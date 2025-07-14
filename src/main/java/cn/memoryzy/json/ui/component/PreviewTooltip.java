package cn.memoryzy.json.ui.component;

import com.intellij.ide.HelpTooltip;
import com.intellij.ide.TooltipTitle;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScreenUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.BrowserLink;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.Alarm;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBValue;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.accessibility.ScreenReader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import static com.intellij.openapi.util.text.HtmlChunk.html;

/**
 * @author Memory
 * @since 2025/7/14
 */
public class PreviewTooltip {

    private static final Color INFO_COLOR = JBColor.namedColor("ToolTip.infoForeground", UIUtil.getContextHelpForeground());

    private static final JBValue MAX_WIDTH = new JBValue.UIInteger("HelpTooltip.maxWidth", 250);
    private static final JBValue X_OFFSET = new JBValue.UIInteger("HelpTooltip.xOffset", 0);
    private static final JBValue Y_OFFSET = new JBValue.UIInteger("HelpTooltip.yOffset", 0);
    private static final JBValue HEADER_FONT_SIZE_DELTA = new JBValue.UIInteger("HelpTooltip.fontSizeDelta", 0);
    private static final JBValue DESCRIPTION_FONT_SIZE_DELTA = new JBValue.UIInteger("HelpTooltip.descriptionSizeDelta", 0);
    private static final JBValue CURSOR_OFFSET = new JBValue.UIInteger("HelpTooltip.mouseCursorOffset", 20);

    private static final String PARAGRAPH_SPLITTER = "<p/?>";
    private static final String TOOLTIP_PROPERTY = "JComponent.PreviewTooltip";

    private @TooltipTitle String title;
    private @NlsSafe String shortcut;
    private @NlsContexts.Tooltip String description;
    private ActionLink link;
    private String previewText;
    private boolean neverHide;
    private @NotNull PreviewTooltip.Alignment alignment = PreviewTooltip.Alignment.CURSOR;

    private BooleanSupplier masterPopupOpenCondition;

    protected ComponentPopupBuilder myPopupBuilder;
    private Dimension myPopupSize;
    private JBPopup myPopup;
    private final Alarm popupAlarm = new Alarm();
    private boolean isOverPopup;
    private boolean isMultiline;
    private int myInitialDelay = -1;
    private int myHideDelay = -1;
    private String myToolTipText;
    private boolean initialShowScheduled;

    protected MouseAdapter myMouseListener;

    private boolean isMouseOverTarget = false; // 新增：标记鼠标是否在目标组件上
    private boolean isMouseOverPopup = false; // 已存在
    private final Alarm mouseCheckAlarm = new Alarm(); // 新增：用于鼠标位置检测的定时器

    /**
     * Location of the HelpTooltip relatively to the owner component.
     */
    public enum Alignment {
        RIGHT {
            @Override public Point getPointFor(Component owner, Dimension popupSize, Point mouseLocation) {
                Dimension size = owner.getSize();
                return new Point(size.width + JBUIScale.scale(5) - X_OFFSET.get(), JBUIScale.scale(1) + Y_OFFSET.get());
            }
        },

        LEFT {
            @Override public Point getPointFor(Component owner, Dimension popupSize, Point mouseLocation) {
                return new Point(- popupSize.width - JBUIScale.scale(5) + X_OFFSET.get(), JBUIScale.scale(1) + Y_OFFSET.get());
            }
        },

        BOTTOM {
            @Override public Point getPointFor(Component owner, Dimension popupSize, Point mouseLocation) {
                Dimension size = owner.getSize();
                return new Point(JBUIScale.scale(1) + X_OFFSET.get(), JBUIScale.scale(5) + size.height - Y_OFFSET.get());
            }
        },

        HELP_BUTTON {
            @Override public Point getPointFor(Component owner, Dimension popupSize, Point mouseLocation) {
                Insets i  = ((JComponent)owner).getInsets();
                return new Point(X_OFFSET.get() - JBUIScale.scale(40), i.top + Y_OFFSET.get() - JBUIScale.scale(6) - popupSize.height);
            }
        },

        CURSOR {
            @Override public Point getPointFor(Component owner, Dimension popupSize, Point mouseLocation) {
                Point location = mouseLocation.getLocation();
                location.y += CURSOR_OFFSET.get();

                SwingUtilities.convertPointToScreen(location, owner);
                Rectangle r = new Rectangle(location, popupSize);
                ScreenUtil.fitToScreen(r);
                location = r.getLocation();
                SwingUtilities.convertPointFromScreen(location, owner);
                r.setLocation(location);

                if (r.contains(mouseLocation)) {
                    location.y = mouseLocation.y - r.height - JBUI.scale(5);
                }

                return location;
            }
        };

        public abstract Point getPointFor(Component owner, Dimension popupSize, Point mouseLocation);
    }

    /**
     * Sets tooltip title. If it's longer than 2 lines (fitting in 250 pixels each) then
     * the text is automatically stripped to the word boundary and dots are added to the end.
     *
     * @param title text for title.
     * @return {@code this}
     */
    public PreviewTooltip setTitle(@Nullable @TooltipTitle String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets text for the shortcut placeholder.
     *
     * @param shortcut text for shortcut.
     * @return {@code this}
     */
    public PreviewTooltip setShortcut(@Nullable @NlsSafe String shortcut) {
        this.shortcut = shortcut;
        return this;
    }

    public PreviewTooltip setShortcut(@Nullable Shortcut shortcut) {
        this.shortcut = shortcut == null ? null : KeymapUtil.getShortcutText(shortcut);
        return this;
    }

    public PreviewTooltip setPreviewText(String previewText) {
        this.previewText = previewText;
        return this;
    }

    /**
     * Set HelpTooltip initial delay. Tooltip is show after component's mouse enter plus initial delay.
     * @param delay - non negative value for initial delay
     * @return {@code this}
     * @throws IllegalArgumentException if delay is less than zero
     */
    public PreviewTooltip setInitialDelay(int delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("Negative delay is not allowed");
        }

        myInitialDelay = delay;
        return this;
    }

    /**
     * Set HelpTooltip hide delay. Tooltip is hidden after component's mouse exit plus hide delay.
     * @param delay - non negative value for hide delay
     * @return {@code this}
     * @throws IllegalArgumentException if delay is less than zero
     */
    public PreviewTooltip setHideDelay(int delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("Negative delay is not allowed");
        }

        myHideDelay = delay;
        return this;
    }

    /**
     * Sets description text.
     *
     * @param description text for description.
     * @return {@code this}
     */
    public PreviewTooltip setDescription(@Nullable @NlsContexts.Tooltip String description) {
        this.description = description;
        return this;
    }

    /**
     * Enables link in the tooltip below description and sets action for it.
     *
     * @param linkText text to show in the link.
     * @param linkAction action to execute when link is clicked.
     * @return {@code this}
     */
    public PreviewTooltip setLink(@NlsContexts.LinkLabel String linkText, Runnable linkAction) {
        return setLink(linkText, linkAction, false);
    }

    /**
     * Enables link in the tooltip below description and sets action for it.
     *
     * @param linkText text to show in the link.
     * @param linkAction action to execute when link is clicked.
     * @param external whether the link is "external" or not
     * @return {@code this}
     */
    public PreviewTooltip setLink(@NlsContexts.LinkLabel String linkText, Runnable linkAction, boolean external) {
        link = new ActionLink(linkText, e -> {
            hidePopup(true);
            linkAction.run();
        });
        if (external) {
            link.setExternalLinkIcon();
        }
        return this;
    }

    /**
     * Enables link in the tooltip below description and sets BrowserUtil.browse action for it.
     * It's then painted with a small arrow button.
     *
     * @param linkLabel text to show in the link.
     * @param url URL to browse.
     * @return {@code this}
     */
    public PreviewTooltip setBrowserLink(@NlsContexts.LinkLabel String linkLabel, URL url) {
        link = new BrowserLink(linkLabel, url.toExternalForm());
        link.setHorizontalTextPosition(SwingConstants.LEFT);
        return this;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        PreviewTooltip tooltip = (PreviewTooltip)that;
        return neverHide == tooltip.neverHide &&
                Objects.equals(title, tooltip.title) &&
                Objects.equals(shortcut, tooltip.shortcut) &&
                Objects.equals(description, tooltip.description) &&
                Objects.equals(link, tooltip.link) &&
                alignment == tooltip.alignment &&
                Objects.equals(masterPopupOpenCondition, tooltip.masterPopupOpenCondition);
    }

    /**
     * Toggles whether to hide tooltip automatically on timeout. For default behaviour just don't call this method.
     *
     * @param neverHide {@code true} don't hide, {@code false} otherwise.
     * @return {@code this}
     */
    public PreviewTooltip setNeverHideOnTimeout(boolean neverHide) {
        this.neverHide = neverHide;
        return this;
    }

    /**
     * Sets location of the tooltip relatively to the owner component.
     *
     * @param alignment is relative location
     * @return {@code this}
     */
    public PreviewTooltip setLocation(@NotNull PreviewTooltip.Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    /**
     * Installs the tooltip after the configuration has been completed on the specified owner component.
     *
     * @param component is the owner component for the tooltip.
     */
    public void installOn(@NotNull JComponent component) {
        PreviewTooltip installed = (PreviewTooltip)component.getClientProperty(TOOLTIP_PROPERTY);

        if (installed == null) installImpl(component);
        else if (!equals(installed)) {
            installed.hideAndDispose(component);
            installImpl(component);
        }
    }

    private void installImpl(@NotNull JComponent component) {
        neverHide = neverHide || UIUtil.isHelpButton(component);

        createMouseListeners();
        initPopupBuilder();

        component.putClientProperty(TOOLTIP_PROPERTY, this);
        installMouseListeners(component);
    }

    protected final void createMouseListeners() {
        myMouseListener = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                isMouseOverTarget = true; // 标记鼠标在目标组件上
                if (myPopup != null && !myPopup.isDisposed()){
                    myPopup.cancel();
                }
                initialShowScheduled = true;
                int delay = myInitialDelay;
                if (delay == -1) {
                    delay = Registry.intValue("ide.tooltip.initialReshowDelay", 500);
                }
                scheduleShow(e, delay);
            }

            @Override public void mouseExited(MouseEvent e) {
                isMouseOverTarget = false; // 标记鼠标离开目标组件
                // int delay = myHideDelay;
                // if (delay == -1) {
                //     delay = Registry.intValue("ide.tooltip.initialDelay.highlighter", 150);
                // }
                // scheduleHide(link == null, delay);
                // 不再立即隐藏，而是启动鼠标位置检测
                checkMousePosition();
            }

            @Override public void mouseMoved(MouseEvent e) {
                if (!initialShowScheduled) {
                    scheduleShow(e, Registry.intValue("ide.tooltip.reshowDelay"));
                }
            }
        };
    }

    private void initPopupBuilder() {
        JComponent tipPanel = createTipPanel();
        tipPanel.addMouseListener(createIsOverTipMouseListener());

        myPopupSize = tipPanel.getPreferredSize();
        myPopupBuilder = JBPopupFactory.getInstance().
                createComponentPopupBuilder(tipPanel, null).
                setShowBorder(UIManager.getBoolean("ToolTip.paintBorder")).
                setBorderColor(JBUI.CurrentTheme.Tooltip.borderColor()).setShowShadow(true);
    }

    protected void initPopupBuilder(@NotNull PreviewTooltip instance) {
        instance.initPopupBuilder();
        myPopupSize = instance.myPopupSize;
        myPopupBuilder = instance.myPopupBuilder;
        initialShowScheduled = false;
    }

    @NotNull
    private MouseListener createIsOverTipMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isMouseOverPopup = true;
                // isOverPopup = true;
                // 取消任何挂起的隐藏操作
                popupAlarm.cancelAllRequests();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // if (link == null || !link.getBounds().contains(e.getPoint())) {
                //     isOverPopup = false;
                //     hidePopup(false);
                // }

                // 不再立即设置isOverPopup为false
                // 而是启动鼠标位置检测
                checkMousePosition();
            }
        };
    }

    // 新增：检查鼠标位置并决定是否隐藏工具提示
    private void checkMousePosition() {
        mouseCheckAlarm.cancelAllRequests();
        mouseCheckAlarm.addRequest(() -> {
            if (!isMouseOverTarget() && !isMouseOverPopup()) {
                // 如果鼠标既不在目标组件上也不在弹出窗口上
                hidePopup(true);
            } else {
                // 如果鼠标仍在区域中，继续检查
                checkMousePosition();
            }
        }, 100); // 每100毫秒检查一次
    }

    // 新增：检查鼠标是否在目标组件上
    private boolean isMouseOverTarget() {
        if (myMouseListener == null) return false;

        // 获取目标组件
        Component target = null;
        if (myPopup != null && myPopup.isVisible()) {
            Point popupPoint = myPopup.getLocationOnScreen();
            Point targetLocation = myPopup.getContent().getLocationOnScreen();
            Dimension targetSize = myPopup.getContent().getSize();

            // 检查鼠标是否在弹出窗口内
            return isMouseInArea(popupPoint, targetLocation, targetSize);
        }
        return false;
    }

    // 新增：检查鼠标是否在弹出窗口上
    private boolean isMouseOverPopup() {
        if (myPopup == null || !myPopup.isVisible()) return false;

        // 获取鼠标当前位置
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) return false;

        Point mousePoint = pointerInfo.getLocation();
        Point popupPoint = myPopup.getContent().getLocationOnScreen();
        Dimension popupSize = myPopup.getContent().getSize();

        // 检查鼠标是否在弹出窗口内
        return mousePoint.x >= popupPoint.x &&
                mousePoint.y >= popupPoint.y &&
                mousePoint.x <= popupPoint.x + popupSize.width &&
                mousePoint.y <= popupPoint.y + popupSize.height;
    }

    // 新增：检查鼠标是否在指定区域内
    private boolean isMouseInArea(Point mousePoint, Point areaPoint, Dimension areaSize) {
        return mousePoint.x >= areaPoint.x &&
                mousePoint.y >= areaPoint.y &&
                mousePoint.x <= areaPoint.x + areaSize.width &&
                mousePoint.y <= areaPoint.y + areaSize.height;
    }

    @NotNull
    protected JPanel createTipPanel() {
        JPanel tipPanel = new JPanel();
        tipPanel.setLayout(new VerticalLayout(JBUI.getInt("HelpTooltip.verticalGap", 4)));
        tipPanel.setBackground(UIUtil.getToolTipBackground());

        boolean hasTitle = StringUtil.isNotEmpty(title);
        boolean hasDescription = StringUtil.isNotEmpty(description);

        if (hasTitle) {
            tipPanel.add(new PreviewTooltip.Header(hasDescription), VerticalLayout.TOP);
        }

        if (hasDescription) {
            @Nls String[] pa = description.split(PARAGRAPH_SPLITTER);
            isMultiline = pa.length > 1;
            Arrays.stream(pa).filter(p -> !p.isEmpty()).forEach(p -> tipPanel.add(new PreviewTooltip.Paragraph(p, hasTitle), VerticalLayout.TOP));
        }

        if (!hasTitle && StringUtil.isNotEmpty(shortcut)) {
            JLabel shortcutLabel = new JLabel(shortcut);
            shortcutLabel.setFont(deriveDescriptionFont(shortcutLabel.getFont(), false));
            shortcutLabel.setForeground(JBUI.CurrentTheme.Tooltip.shortcutForeground());

            tipPanel.add(shortcutLabel, VerticalLayout.TOP);
        }

        if (link != null) {
            link.setFont(deriveDescriptionFont(link.getFont(), hasTitle));
            tipPanel.add(link, VerticalLayout.TOP);
        }

        // 添加分割线和预览窗口
        if (shouldShowPreview()) {
            // 添加分割线
            JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
            separator.setForeground(JBUI.CurrentTheme.Tooltip.borderColor());
            separator.setBorder(JBUI.Borders.empty(8, 0));
            tipPanel.add(separator, VerticalLayout.TOP);

            // 添加预览标题
            JBLabel previewLabel = new JBLabel("JSON 预览:");
            previewLabel.setFont(previewLabel.getFont().deriveFont(Font.BOLD, 12f));
            previewLabel.setForeground(JBUI.CurrentTheme.Tooltip.shortcutForeground());
            previewLabel.setBorder(JBUI.Borders.empty(0, 0, 4, 0));
            tipPanel.add(previewLabel, VerticalLayout.TOP);

            // 添加预览窗口
            JComponent previewPanel = createJsonPreviewPanel();
            tipPanel.add(previewPanel, VerticalLayout.TOP);
        }

        isMultiline = isMultiline || StringUtil.isNotEmpty(description) && (StringUtil.isNotEmpty(title) || link != null);
        tipPanel.setBorder(textBorder(isMultiline));

        return tipPanel;
    }

    private boolean shouldShowPreview() {
        return StringUtil.isNotEmpty(previewText) && previewText.length() > 10;
    }

    @NotNull
    private JComponent createJsonPreviewPanel() {
        // 创建高亮组件
        JsonHighlighterPane highlighter = new JsonHighlighterPane(previewText);
        // 创建带滚动条的面板
        return highlighter.createScrollPane();
    }

    private void installMouseListeners(@NotNull JComponent owner) {
        owner.addMouseListener(myMouseListener);
        owner.addMouseMotionListener(myMouseListener);
    }

    private void uninstallMouseListeners(@NotNull JComponent owner) {
        owner.removeMouseListener(myMouseListener);
        owner.removeMouseMotionListener(myMouseListener);
    }

    /**
     * Hides and disposes the tooltip possibly installed on the mentioned component. Disposing means
     * unregistering all {@code HelpTooltip} specific listeners installed on the component.
     * If there is no tooltip installed on the component nothing happens.
     *
     * @param owner a possible {@code HelpTooltip} owner.
     */
    public static void dispose(@NotNull Component owner) {
        if (owner instanceof JComponent) {
            JComponent component = (JComponent)owner;
            PreviewTooltip instance = (PreviewTooltip)component.getClientProperty(TOOLTIP_PROPERTY);
            if (instance != null) {
                instance.hideAndDispose(component);
            }
        }
    }

    private void hideAndDispose(@NotNull JComponent owner) {
        hidePopup(true);
        uninstallMouseListeners(owner);
        masterPopupOpenCondition = null;
        owner.putClientProperty(TOOLTIP_PROPERTY, null);
    }

    /**
     * Hides the tooltip possibly installed on the mentioned component without disposing.
     * Listeners are not removed.
     * If there is no tooltip installed on the component nothing happens.
     *
     * @param owner a possible {@code HelpTooltip} owner.
     */
    public static void hide(@NotNull Component owner) {
        if (owner instanceof JComponent) {
            PreviewTooltip instance = (PreviewTooltip)((JComponent)owner).getClientProperty(TOOLTIP_PROPERTY);
            if (instance != null) {
                instance.hidePopup(true);
            }
        }
    }

    /**
     * Sets master popup for the current {@code HelpTooltip}. Master popup takes over the help tooltip,
     * so when the master popup is about to be shown help tooltip hides.
     *
     * @param owner possible owner
     * @param master master popup
     */
    public static void setMasterPopup(@NotNull Component owner, JBPopup master) {
        if (owner instanceof JComponent) {
            PreviewTooltip instance = (PreviewTooltip)((JComponent)owner).getClientProperty(TOOLTIP_PROPERTY);
            if (instance != null && instance.myPopup != master) {
                instance.masterPopupOpenCondition = () -> master == null || !master.isVisible();
            }
        }
    }

    /**
     * Sets master popup open condition supplier for the current {@code HelpTooltip}.
     * This method is more general than {@link HelpTooltip#setMasterPopup(Component, JBPopup)} so that
     * it's possible to create master popup condition for any types of popups such as {@code JPopupMenu}
     *
     * @param owner possible owner
     * @param condition a {@code BooleanSupplier} for open condition
     */
    public static void setMasterPopupOpenCondition(@NotNull Component owner, @Nullable BooleanSupplier condition) {
        if (owner instanceof JComponent) {
            PreviewTooltip instance = (PreviewTooltip)((JComponent)owner).getClientProperty(TOOLTIP_PROPERTY);
            if (instance != null) {
                instance.masterPopupOpenCondition = condition;
            }
        }
    }

    private void scheduleShow(MouseEvent e, int delay) {
        popupAlarm.cancelAllRequests();

        if (ScreenReader.isActive()) return; // Disable HelpTooltip in screen reader mode.

        popupAlarm.addRequest(() -> {
            initialShowScheduled = false;
            if (masterPopupOpenCondition == null || masterPopupOpenCondition.getAsBoolean()) {
                Component owner = e.getComponent();
                String text = owner instanceof JComponent ? ((JComponent)owner).getToolTipText(e) : null;
                if (myPopup != null && !myPopup.isDisposed()) {
                    if (StringUtil.isEmpty(text) && StringUtil.isEmpty(myToolTipText)) return; // do nothing if a tooltip become empty
                    if (StringUtil.equals(text, myToolTipText)) return; // do nothing if a tooltip is not changed
                    myPopup.cancel(); // cancel previous popup before showing a new one
                }
                myToolTipText = text;
                myPopup = myPopupBuilder.createPopup();
                myPopup.show(new RelativePoint(owner, alignment.getPointFor(owner, myPopupSize, e.getPoint())));
                if (!neverHide) {
                    int dismissDelay = Registry.intValue(isMultiline ? "ide.helptooltip.full.dismissDelay" : "ide.helptooltip.regular.dismissDelay");
                    scheduleHide(true, dismissDelay);
                }
            }
        }, delay);
    }

    private void scheduleHide(boolean force, int delay) {
        popupAlarm.cancelAllRequests();
        popupAlarm.addRequest(() -> hidePopup(force), delay);
    }

    protected void hidePopup(boolean force) {
        initialShowScheduled = false;
        popupAlarm.cancelAllRequests();
        mouseCheckAlarm.cancelAllRequests(); // 取消鼠标检查

        if (myPopup != null && myPopup.isVisible() && (force || (!isMouseOverTarget && !isMouseOverPopup))) {
            myPopup.cancel();
            myPopup = null;
            myToolTipText = null;
            isMouseOverPopup = false;
        }
    }

    private static Border textBorder(boolean multiline) {
        Insets i = UIManager.getInsets(multiline ? "HelpTooltip.defaultTextBorderInsets" : "HelpTooltip.smallTextBorderInsets");
        return i != null ? new JBEmptyBorder(i) : JBUI.Borders.empty();
    }

    private static Font deriveHeaderFont(Font font) {
        return font.deriveFont((float)font.getSize() + HEADER_FONT_SIZE_DELTA.get());
    }

    private static Font deriveDescriptionFont(Font font, boolean hasTitle) {
        return hasTitle ?
                font.deriveFont((float)font.getSize() + DESCRIPTION_FONT_SIZE_DELTA.get()) :
                deriveHeaderFont(font);
    }

    @Contract(pure = true)
    public static @NotNull String getShortcutAsHtml(@Nullable String shortcut) {
        return StringUtil.isEmpty(shortcut)
                ? ""
                : String.format("&nbsp;&nbsp;<font color=\"%s\">%s</font>", ColorUtil.toHtmlColor(JBUI.CurrentTheme.Tooltip.shortcutForeground()),
                shortcut);
    }

    private static class BoundWidthLabel extends JLabel {
        private static Collection<View> getRows(@NotNull View root) {
            Collection<View> rows = new ArrayList<>();
            visit(root, rows);
            return rows;
        }

        private static void visit(@NotNull View v, Collection<? super View> result) {
            String cname = v.getClass().getCanonicalName();
            if (cname != null && cname.contains("ParagraphView.Row")) {
                result.add(v);
            }

            for(int i = 0; i < v.getViewCount(); i++) {
                visit(v.getView(i), result);
            }
        }

        void setSizeForWidth(float width) {
            if (width > MAX_WIDTH.get()) {
                View v = (View)getClientProperty(BasicHTML.propertyKey);
                if (v != null) {
                    width = 0.0f;
                    for(View row : getRows(v)) {
                        float rWidth = row.getPreferredSpan(View.X_AXIS);
                        if (width < rWidth) {
                            width = rWidth;
                        }
                    }

                    v.setSize(width, v.getPreferredSpan(View.Y_AXIS));
                }
            }
        }
    }

    private final class Header extends PreviewTooltip.BoundWidthLabel {
        private Header(boolean obeyWidth) {
            setFont(deriveHeaderFont(getFont()));
            setForeground(UIUtil.getToolTipForeground());

            if (obeyWidth || title.length() > MAX_WIDTH.get()) {
                View v = BasicHTML.createHTMLView(this, String.format("<html>%s%s</html>", title, getShortcutAsHTML()));
                float width = v.getPreferredSpan(View.X_AXIS);
                isMultiline = isMultiline || width > MAX_WIDTH.get();
                HtmlChunk.Element div = width > MAX_WIDTH.get() ? HtmlChunk.div().attr("width", MAX_WIDTH.get()) : HtmlChunk.div();
                setText(div.children(HtmlChunk.raw(title), HtmlChunk.raw(getShortcutAsHTML()))
                        .wrapWith(html())
                        .toString());
                setSizeForWidth(width);
            }
            else {
                setText(BasicHTML.isHTMLString(title) ?
                        title :
                        HtmlChunk.div().addRaw(title).addRaw(getShortcutAsHTML()).wrapWith(html()).toString());
            }
        }

        private @NlsSafe String getShortcutAsHTML() {
            return getShortcutAsHtml(shortcut);
        }
    }

    private final class Paragraph extends PreviewTooltip.BoundWidthLabel {
        private Paragraph(@NlsContexts.Tooltip String text, boolean hasTitle) {
            setForeground(hasTitle ? INFO_COLOR : UIUtil.getToolTipForeground());
            setFont(deriveDescriptionFont(getFont(), hasTitle));

            View v = BasicHTML.createHTMLView(this, HtmlChunk.raw(text).wrapWith(html()).toString());
            float width = v.getPreferredSpan(View.X_AXIS);
            isMultiline = isMultiline || width > MAX_WIDTH.get();
            HtmlChunk.Element div = width > MAX_WIDTH.get() ? HtmlChunk.div().attr("width", MAX_WIDTH.get()) : HtmlChunk.div();
            setText(div.addRaw(text).wrapWith(html()).toString());

            setSizeForWidth(width);
        }
    }
    
}
