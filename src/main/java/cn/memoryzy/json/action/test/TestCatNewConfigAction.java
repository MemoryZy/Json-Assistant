package cn.memoryzy.json.action.test;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.HistoryTreeNodeType;
import cn.memoryzy.json.ui.tree.HistoryFilterableTree;
import cn.memoryzy.json.ui.tree.HistoryNode;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * @author Memory
 * @since 2025/6/23
 */
public class TestCatNewConfigAction extends DumbAwareAction implements UpdateInBackground {

    public TestCatNewConfigAction() {
        super("测试配置查看");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();


//        BlacklistManager blacklistManager = BlacklistManager.getInstance();
//
//        GeneralSettings generalSettings = GeneralSettings.getInstance();
//
//        HistoryManager historyManager = HistoryManager.getInstance(getEventProject(e));
//
//        SerializationSettings serializationSettings = SerializationSettings.getInstance();
//
//        ToolWindowSettings toolWindowSettings = ToolWindowSettings.getInstance();
//
//        JsonRecord jsonRecord = new JsonRecord()
//                .setId(123);
//
//        historyManager.addEntry(jsonRecord);

//        MessageBus messageBus = e.getProject().getMessageBus();
//
//        RefreshFloatToolbarEvent refreshFloatToolbarEvent = messageBus.syncPublisher(RefreshFloatToolbarEvent.TOPIC);
//
//        System.out.println("["  + Thread.currentThread().getName() + "] send");
//
//        refreshFloatToolbarEvent.accept(null);

        // MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        //
        // ColorSchemeChangedEvent colorSchemeChangedEvent = messageBus.syncPublisher(ColorSchemeChangedEvent.TOPIC);
        //
        // colorSchemeChangedEvent.change(ColorScheme.Classic);

        // HistoryToolWindowManager manager = HistoryToolWindowManager.getInstance(getEventProject(e));
        // manager.show();


        // List<String> list = List.of("航班监控", "航班设置", "航班注释", "Search");


        // TextFieldWithAutoCompletion<String> completion = TextFieldWithAutoCompletion.create(
        //         getEventProject(e),
        //         list,
        //         null,
        //         true,
        //         "kkkk"
        // );


        // BorderLayoutPanel panel = new BorderLayoutPanel().addToCenter(completion);
        //
        // new DialogBuilder()
        //         .centerPanel(panel)
        //                 .show();

        // Project project = getEventProject(e);
        // PsiFile tempPsiFile = PsiFileFactory.getInstance(project)
        //         .createFileFromText("temp." + Json5FileType.INSTANCE.getDefaultExtension(), Json5FileType.INSTANCE, "{\n" +
        //                 "            \"label\": \"Learn More\",\n" +
        //                 "            \"url\": \"https://en.example.com/notice\",\n" +
        //                 "            \"command\": \"\"\n" +
        //                 "          }");
        //
        // PsiElement[] children = tempPsiFile.getChildren();
        //
        //
        // tempPsiFile.accept(new JsonRecursiveElementVisitor() {
        //     @Override
        //     public void visitObject(@NotNull JsonObject o) {
        //         super.visitObject(o);
        //         List<JsonProperty> propertyList = o.getPropertyList();
        //         for (JsonProperty property : propertyList) {
        //             JsonValue jsonValue = property.getValue();
        //             // handleElement(project, jsonValue, handleType);
        //             String name = property.getName();
        //
        //
        //             System.out.println();
        //         }
        //     }
        //
        //     @Override
        //     public void visitArray(@NotNull JsonArray o) {
        //         super.visitArray(o);
        //         List<JsonValue> valueList = o.getValueList();
        //         for (JsonValue jsonValue : valueList) {
        //
        //
        //             // handleElement(project, jsonValue, handleType);
        //         }
        //     }
        // });
        //
        //
        // System.out.println(tempPsiFile.getText());
        //
        // JsonObject jsonObject = (JsonObject) tempPsiFile.getChildren()[0];
        //
        // JsonProperty label = jsonObject.findProperty("label");
        //
        // JsonValue value = label.getValue();
        //
        //
        // JsonElementGenerator generator = new JsonElementGenerator(project);
        // JsonValue jsonValue = generator.createValue("\"hahahaha\"");
        //
        // value.replace(jsonValue);
        //
        // WriteCommandAction.runWriteCommandAction(
        //         project,
        //         () -> CodeStyleManager.getInstance(project).reformatText(tempPsiFile, 0, tempPsiFile.getText().length()));
        //
        //
        // System.out.println(tempPsiFile.getText());


        //

        // JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 右对齐FlowLayout
        // JCheckBox checkBox = new JCheckBox("同意条款");
        // flowPanel.add(checkBox);
        //
        // // ExpandableEditorTextField editorTextField = new ExpandableEditorTextField(JsonLanguage.INSTANCE);
        //
        // // ExpandableEditorProvider expandableEditorProvider = new ExpandableEditorProvider(e.getProject());
        //
        // // ExpandableLanguageTextField expandableLanguageTextField = new ExpandableLanguageTextField(project, Json5Language.INSTANCE, "");
        //
        // // EmbeddedButtonLanguageTextField embeddedButtonLanguageTextField = new EmbeddedButtonLanguageTextField(project, PlainTextLanguage.INSTANCE, "");
        //
        // BorderLayoutPanel panel = new BorderLayoutPanel()
        //         // .addToCenter(expandableLanguageTextField)
        //         // .addToCenter(expandableEditorProvider.createComponent())
        //         // .addToCenter(embeddedButtonLanguageTextField)
        //         .addToBottom(flowPanel);
        //
        //
        // new DialogBuilder()
        //         .centerPanel(panel)
        //         .show();





        // boolean ask = MessageDialogBuilder.okCancel(JsonAssistantBundle.messageOnSystem("dialog.clear.editor.title"), JsonAssistantBundle.messageOnSystem("dialog.clear.editor.content"))
        //         .icon(Messages.getWarningIcon())
        //         .doNotAsk(new DoNotAskOption.Adapter() {
        //             @Override
        //             public void rememberChoice(boolean isSelected, int exitCode) {
        //                 // 点击确定
        //                 if (MessageConstants.YES == exitCode && isSelected) {
        //
        //                 }
        //
        //
        //                 System.out.println();
        //             }
        //
        //             @Override
        //             public @NotNull String getDoNotShowMessage() {
        //                 return JsonAssistantBundle.messageOnSystem("dialog.options.do.not.ask");
        //             }
        //         })
        //
        //         .ask(project);


        // ShowSettingsUtilImpl.showSettingsDialog(project, JsonAssistantMainConfigurable.ID, null);

        // ShowStructureSettingsAction

        // ShowSettingsUtilImpl.showSettingsDialog(e.getProject(), PluginManagerConfigurable.ID, JsonAssistantPlugin.PLUGIN_NAME);

        // SettingsDialogFactory.getInstance().create(
        //         currentOrDefaultProject(project),
        //         Collections.singletonList(group),
        //         configurableToSelect,
        //         filter
        // ).show();


        // ShowSettingsUtil.getInstance().showSettingsDialog(project,
        //         configurable -> configurable instanceof JsonAssistantMainConfigurable,
        //         configurable -> {
        //
        //             System.out.println();
        //         });

                // ParallelLabelDialog dialog = new ParallelLabelDialog(
                //     "确认操作",
                //     "您确定要执行此操作吗？此操作不可逆。",
                //     Messages.getQuestionIcon()
                // );

                // dialog.setUndecorated(true);

        // OkCancelDialog dialog = new OkCancelDialog(
        //         JsonAssistantBundle.messageOnSystem("dialog.clear.editor.title"),
        //         JsonAssistantBundle.messageOnSystem("dialog.clear.editor.content"),
        //             Messages.getWarningIcon());
        //
        // if (dialog.showAndGet()) {
        //
        // }


        // IdeaPluginDescriptor[] plugins = PluginManager.getPlugins();
        //
        // for (IdeaPluginDescriptor plugin : plugins) {
        //     PluginId pluginId = plugin.getPluginId();
        //     System.out.println(pluginId);
        //
        // }
        //
        // JavaUtil.hasJavaOrKotlinEnvironment();


        // ExtendableTextField extendableTextField = new ExtendableTextField(15);
        // extendableTextField.setExtensions(new EditExtension());
        //
        // extendableTextField.setOpaque(false);
        // extendableTextField.setBorder(JBUI.Borders.empty());
        //
        //
        // BorderLayoutPanel panel = new BorderLayoutPanel().addToCenter(extendableTextField);
        //
        // new DialogBuilder()
        //         .centerPanel(panel)
        //         .show();


        // StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        // StatusBarWidget widget = statusBar.getWidget("Position");

        HistoryFilterableTree filterableTree = new HistoryFilterableTree(project, new HistoryNode().setNodeType(HistoryTreeNodeType.ROOT));
        Tree tree = filterableTree.getTree();
        filterableTree.installSimple();

        tree.setRootVisible(false);

        tree.setCellRenderer(new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                HistoryNode historyNode = (HistoryNode) node.getUserObject();
                HistoryTreeNodeType nodeType = historyNode.getNodeType();

                if (HistoryTreeNodeType.NODE.equals(nodeType)) {
                    setIcon(AllIcons.FileTypes.Json);
                    append(historyNode.toString());
                } else {
                    // setIcon(JsonAssistantIcons.GROUP);
                    setIcon(JsonAssistantIcons.ToolWindow.MODULE);
                    append(historyNode + " (" + historyNode.getSize() + ")");
                }

                if (!tree.isEnabled()) {
                    setEnabled(false);
                    tree.setForeground(JBColor.GRAY);
                    tree.setToolTipText(JsonAssistantBundle.messageOnSystem("tooltip.history.tree.disabled.text"));
                } else {
                    if (!isEnabled()) setEnabled(true);
                    tree.setForeground(UIUtil.getTreeForeground());
                    tree.setToolTipText(null);
                }
            }
        });

        JScrollPane scrollPane = UIUtils.wrapScrollPane(tree);


        // new TreeSpeedSearch(tree).setClearSearchOnNavigateNoMatch(true);




        BorderLayoutPanel panel = new BorderLayoutPanel().addToCenter(scrollPane);

        new DialogBuilder()
                .centerPanel(panel)
                .show();

        
        System.out.println();

    }




    protected static final Border BORDER = new CustomLineBorder(JBColor.namedColor("SpeedSearch.borderColor", JBColor.LIGHT_GRAY), JBUI.insets(1));
    protected static final Color FOREGROUND_COLOR = JBColor.namedColor("SpeedSearch.foreground", UIUtil.getToolTipForeground());
    protected static final Color BACKGROUND_COLOR = JBColor.namedColor("SpeedSearch.background", new JBColor(Gray.xFF, Gray._111));
    protected static final Color ERROR_FOREGROUND_COLOR = JBColor.namedColor("SpeedSearch.errorForeground", JBColor.RED);

    protected class SearchPopup extends JPanel {
        protected final SearchField mySearchField;
        private String myLastPattern = "";

        protected SearchPopup(String initialString) {
            mySearchField = new SearchField();

            mySearchField.setBorder(null);
            mySearchField.setBackground(BACKGROUND_COLOR);
            mySearchField.setForeground(FOREGROUND_COLOR);
            //
            // mySearchField.setDocument(new PlainDocument() {
            //     @Override
            //     public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            //         String oldText;
            //         try {
            //             oldText = getText(0, getLength());
            //         }
            //         catch (BadLocationException e1) {
            //             oldText = "";
            //         }
            //
            //         String newText = oldText.substring(0, offs) + str + oldText.substring(offs);
            //         super.insertString(offs, str, a);
            //         handleInsert(newText);
            //     }
            // });

            setBorder(BORDER);
            setBackground(BACKGROUND_COLOR);
            setLayout(new BorderLayout());
            add(mySearchField, BorderLayout.CENTER);
            mySearchField.setText(initialString);

        }
    }


    protected class SearchField extends ExtendableTextField {
        SearchField() {
            // setFocusable(false);
            ExtendableTextField.Extension leftExtension = new Extension() {
                @Override
                public Icon getIcon(boolean hovered) {
                    return AllIcons.Actions.Search;
                }

                @Override
                public boolean isIconBeforeText() {
                    return true;
                }

                @Override
                public int getIconGap() {
                    return JBUIScale.scale(10);
                }
            };

            addExtension(leftExtension);
        }

        @Override
        public void setForeground(Color color) {
            super.setForeground(color);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize();
            Insets m = getMargin();
            dim.width = getFontMetrics(getFont()).stringWidth(getText()) + 10 + m.left + m.right;
            return dim;
        }


    }

}
