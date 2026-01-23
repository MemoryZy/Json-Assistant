package cn.memoryzy.json.action.test;

import com.intellij.configurationStore.StateStorageManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.components.impl.stores.IProjectStore;
import com.intellij.openapi.project.Project;
import com.intellij.project.ProjectKt;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * @author Memory
 * @since 2025/6/23
 */
public class TestGlobalAction extends BaseTestDumbAwareAction {

    @Override
    public String getActionId() {
        return "JsonAssistant.Action.TestGlobalAction";
    }

    @Override
    public String getActionText() {
        return "全局测试";
    }

    @Override
    public String getParentGroupId() {
        return "HelpMenu";
    }

    @Override
    public Constraints getConstraints() {
        return Constraints.FIRST;
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


        //
        // VirtualFile file = VirtualFileManager.getInstance().findFileByNioPath(Path.of("C:\\Users\\86188\\.ideanotes"));
        // LocalFileSystem.getInstance().refreshAndFindFileByNioFile(file.toNioPath());
        //
        // VirtualFile[] children = file.getChildren();
        // VirtualFile child = file.findChild("xx.txt");
        //
        // VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByNioFile(Path.of("C:\\Users\\86188\\.ideanotes\\xx.txt"));
        // VirtualFile virtualFile2 = LocalFileSystem.getInstance().findFileByNioFile(Path.of("C:\\Users\\86188\\.ideanotes\\xx.txt"));
        //
        // String nameWithoutExtension = child.getNameWithoutExtension();
        //
        // List<VirtualFile> virtualFiles = ListUtil.of(children);
        //
        // boolean contains = virtualFiles.contains(virtualFile);
        // boolean contains1 = virtualFiles.contains(virtualFile2);
        //
        // boolean equals1 = child.equals(virtualFile);
        // boolean equals2 = child.equals(virtualFile2);
        // boolean equals3 = virtualFile.equals(virtualFile2);
        //
        // VirtualFile bbFile = LocalFileSystem.getInstance().findFileByNioFile(Path.of("C:\\Users\\86188\\.ideanotes\\cc.txt"));
        // boolean valid = bbFile.isValid();
        //
        // FileUtil.del("C:\\Users\\86188\\.ideanotes\\cc.txt");
        // file.refresh(false, false);
        //
        // boolean valid1 = bbFile.isValid();

        // Path nioPath = child.toNioPath();
        // String path = child.getPath();
        // String canonicalPath = child.getCanonicalPath();
        //
        // File file1 = nioPath.toFile();
        //
        // boolean exists = file1.exists();
        //
        // byte[] bytes = null;
        // try {
        //     bytes = child.contentsToByteArray();
        // } catch (IOException ex) {
        //     throw new RuntimeException(ex);
        // }
        // String s1 = new String(bytes, StandardCharsets.UTF_8);
        //
        // String s = FileUtil.readUtf8String(file1);


        IProjectStore store = ProjectKt.getStateStore(project);
        Path projectBasePath = store.getProjectBasePath();
        Path directoryStorePath = store.getDirectoryStorePath();
        String presentableUrl = store.getPresentableUrl();
        Path projectFilePath = store.getProjectFilePath();
        String projectName = store.getProjectName();
        String projectWorkspaceId = store.getProjectWorkspaceId();
        StorageScheme storageScheme = store.getStorageScheme();
        Path workspacePath = store.getWorkspacePath();

        StateStorageManager storageManager = store.getStorageManager();


        // TODO 如果是文件型的，就用自定义目录，否则在 .idea 下建立文件

        System.out.println();

    }


}
