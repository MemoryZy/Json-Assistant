package cn.memoryzy.json.action.test;

import com.intellij.json.json5.Json5FileType;
import com.intellij.json.psi.*;
import com.intellij.json.psi.impl.JsonRecursiveElementVisitor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

        Project project = getEventProject(e);
        PsiFile tempPsiFile = PsiFileFactory.getInstance(project)
                .createFileFromText("temp." + Json5FileType.INSTANCE.getDefaultExtension(), Json5FileType.INSTANCE, "{\n" +
                        "            \"label\": \"Learn More\",\n" +
                        "            \"url\": \"https://en.example.com/notice\",\n" +
                        "            \"command\": \"\"\n" +
                        "          }");

        PsiElement[] children = tempPsiFile.getChildren();


        tempPsiFile.accept(new JsonRecursiveElementVisitor() {
            @Override
            public void visitObject(@NotNull JsonObject o) {
                super.visitObject(o);
                List<JsonProperty> propertyList = o.getPropertyList();
                for (JsonProperty property : propertyList) {
                    JsonValue jsonValue = property.getValue();
                    // handleElement(project, jsonValue, handleType);
                    String name = property.getName();


                    System.out.println();
                }
            }

            @Override
            public void visitArray(@NotNull JsonArray o) {
                super.visitArray(o);
                List<JsonValue> valueList = o.getValueList();
                for (JsonValue jsonValue : valueList) {


                    // handleElement(project, jsonValue, handleType);
                }
            }
        });


        System.out.println(tempPsiFile.getText());

        JsonObject jsonObject = (JsonObject) tempPsiFile.getChildren()[0];

        JsonProperty label = jsonObject.findProperty("label");

        JsonValue value = label.getValue();


        JsonElementGenerator generator = new JsonElementGenerator(project);
        JsonValue jsonValue = generator.createValue("\"hahahaha\"");

        value.replace(jsonValue);

        WriteCommandAction.runWriteCommandAction(
                project,
                () -> CodeStyleManager.getInstance(project).reformatText(tempPsiFile, 0, tempPsiFile.getText().length()));


        System.out.println(tempPsiFile.getText());


        //


    }
}
