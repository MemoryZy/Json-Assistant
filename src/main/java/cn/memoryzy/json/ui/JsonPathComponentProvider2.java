package cn.memoryzy.json.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionButtonLook;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/12/17
 */
public class JsonPathComponentProvider2 {

    private final Project project;

    public JsonPathComponentProvider2(Project project) {
        this.project = project;
    }

    // TODO 对照 JsonPathEvaluateView 类

    // TODO 实现 filetype 扩展

    // 上面是searchTextField

    public JComponent createComponent() {
        JPanel panel = new JPanel(new BorderLayout());

        JComponent firstComponent = createFirstComponent();

        panel.add(firstComponent, BorderLayout.NORTH);



        return panel;
    }


    /**
     * 创建搜索框组件
     *
     * @return 搜索框组件
     */
    private JComponent createFirstComponent() {
        // SearchWrapper searchWrapper = new SearchWrapper(new BorderLayout());
        // SearchTextField searchTextField = new SearchTextField(project, PlainTextFileType.INSTANCE);
        // // use font as in regular editor
        // searchTextField.setFontInheritedFromLAF(false);
        //
        // ActionButton historyButton = new SearchHistoryButton(new AnAction("xxxxx", null, AllIcons.Actions.SearchWithHistory) {
        //     @Override
        //     public void actionPerformed(@NotNull AnActionEvent e) {
        //
        //     }
        // }, false);
        //
        // JPanel historyButtonWrapper = new NonOpaquePanel(new BorderLayout());
        // historyButtonWrapper.setBorder(JBUI.Borders.empty(3, 6));
        // historyButtonWrapper.add(historyButton, BorderLayout.NORTH);
        //
        // searchWrapper.add(historyButtonWrapper, BorderLayout.WEST);
        // searchWrapper.add(searchTextField, BorderLayout.CENTER);
        // searchWrapper.setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0));
        // searchWrapper.setOpaque(true);
        //
        // return searchWrapper;


        return null;
    }

    private JComponent createSecondComponent() {

        return null;
    }


    public static void main(String[] args) {
        // JBSplitter splitter = new JBSplitter(true, 0.5f);
        // splitter.setFirstComponent(borderLayoutPanel);
        // splitter.setSecondComponent(showTextField);
        //
        // // 保存分割比例
        // splitter.setSplitterProportionKey(showTextField);


    }





    private class SearchHistoryButton extends ActionButton {

        public SearchHistoryButton(@NotNull AnAction action, boolean focusable) {
            super(action, action.getTemplatePresentation().clone(), ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);
            setLook(ActionButtonLook.INPLACE_LOOK);
            setFocusable(focusable);
            updateIcon();
        }

        @Override
        protected DataContext getDataContext() {
            return DataManager.getInstance().getDataContext(this);
        }

        @Override
        public int getPopState() {
            return isSelected() ? ActionButtonComponent.SELECTED : super.getPopState();
        }

        @Override
        public Icon getIcon() {
            if (isEnabled() && isSelected()) {
                Icon selectedIcon = myPresentation.getSelectedIcon();
                if (selectedIcon != null) return selectedIcon;
            }
            return super.getIcon();
        }
    }

}
