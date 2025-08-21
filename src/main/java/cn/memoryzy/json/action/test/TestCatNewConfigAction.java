package cn.memoryzy.json.action.test;

import cn.memoryzy.json.ui.editor.EditExtension;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

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


        ExtendableTextField extendableTextField = new ExtendableTextField(15);
        extendableTextField.setExtensions(new EditExtension());

        extendableTextField.setOpaque(false);
        extendableTextField.setBorder(JBUI.Borders.empty());


        BorderLayoutPanel panel = new BorderLayoutPanel().addToCenter(extendableTextField);

        new DialogBuilder()
                .centerPanel(panel)
                .show();

    }
}
