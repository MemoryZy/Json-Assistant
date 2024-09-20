package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.formats.JsonFormatHandleModel;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class ToXmlAction extends DumbAwareAction {

    private static final Logger LOG = Logger.getInstance(ToXmlAction.class);

    public ToXmlAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.to.xml.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.to.xml.description"));
        presentation.setIcon(XmlFileType.INSTANCE.getIcon());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = PlatformUtil.getEditor(e);
        Document document = editor.getDocument();
        JsonFormatHandleModel model = JsonFormatHandleModel.of(project, editor,
                JsonAssistantBundle.messageOnSystem("hint.select.json.to.xml.text"),
                JsonAssistantBundle.messageOnSystem("hint.all.json.to.xml.text"));

        String xmlStr;
        try {
            xmlStr = JsonUtil.jsonToXml(model.getContent());
            xmlStr = xmlStr.replaceAll("\r\n", "\n");
        } catch (Exception ex) {
            LOG.error("xml conversion failure", ex);
            return;
        }

        JsonAssistantUtil.applyProcessedTextToDocumentOrClipboard(e.getProject(), editor, document, xmlStr,
                model, true, JsonAssistantUtil.isNotWriteXmlDoc(e, project, document, model));
    }

}
