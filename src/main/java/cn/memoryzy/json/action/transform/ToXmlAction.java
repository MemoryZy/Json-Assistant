package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TextTransformUtil;
import cn.memoryzy.json.util.XmlUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
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
        presentation.setIcon(AllIcons.FileTypes.Xml);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // 现在默认是新开窗口展示转换后的文本，所以不需要提示了
        // JsonAssistantBundle.messageOnSystem("hint.selection.json.to.xml.text")
        // JsonAssistantBundle.messageOnSystem("hint.global.json.to.xml.text")
        DataContext dataContext = event.getDataContext();

        String xmlStr;
        try {
            xmlStr = XmlUtil.toXml(
                    GlobalJsonConverter.parseJson(
                            dataContext,
                            PlatformUtil.getEditor(dataContext)));
            xmlStr = xmlStr.replaceAll("\r\n", "\n");
        } catch (Exception ex) {
            LOG.error("xml conversion failure", ex);
            return;
        }

        TextTransformUtil.applyTextWhenNotWritable(getEventProject(event), xmlStr, FileTypeHolder.XML);
    }

}
