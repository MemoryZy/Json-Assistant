package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.ui.extension.SearchExtension;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.fields.ExtendableTextField;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonPathFilterOnTextFieldAction extends DumbAwareAction {

    private final JsonViewerWindow window;

    public JsonPathFilterOnTextFieldAction(JsonViewerWindow window) {
        super(JsonAssistantBundle.messageOnSystem("action.json.path.filter.text"),
                JsonAssistantBundle.messageOnSystem("action.json.path.filter.description"),
                JsonAssistantIcons.FILTER);
        this.window = window;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Component source = (Component) e.getInputEvent().getSource();
        RelativePoint relativePoint = new RelativePoint(source, new Point(-(source.getWidth() * 2), source.getHeight() + 1));
        ExtendableTextField extendableTextField = new ExtendableTextField(20);
        extendableTextField.addExtension(new SearchExtension());
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(extendableTextField, BorderLayout.CENTER);

        JBPopupFactory.getInstance()
                .createComponentPopupBuilder(panel, extendableTextField)
                .setFocusable(true)
                .setTitle(JsonAssistantBundle.messageOnSystem("popup.json.path.filter.on.text.field.title"))
                .setShowShadow(true)
                .setShowBorder(true)
                .setModalContext(false)
                .setLocateWithinScreenBounds(true)
                .setFocusable(true)
                .setRequestFocus(true)
                .setModalContext(false)
                .setCancelOnClickOutside(true)
                .setCancelOnOtherWindowOpen(true)
                .setCancelKeyEnabled(true)
                .setMovable(true)
                .createPopup()
                .show(relativePoint);
    }
}
