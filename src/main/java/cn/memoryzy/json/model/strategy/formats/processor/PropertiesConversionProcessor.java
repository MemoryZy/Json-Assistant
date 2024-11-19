package cn.memoryzy.json.model.strategy.formats.processor;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.data.ActionData;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.data.MessageData;
import cn.memoryzy.json.util.DataConverter;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;

/**
 * @author Memory
 * @since 2024/11/4
 */
public class PropertiesConversionProcessor extends AbstractGlobalTextConversionProcessor {

    public PropertiesConversionProcessor(EditorData editorData) {
        super(editorData, true);
    }

    @Override
    public boolean canConvert(String text) {
        // 增加前置条件：只有在Properties文件内才判断
        return isPropertiesFileContext() && DataConverter.canPropertiesBeConvertedToJson(text);
    }

    @Override
    public String convertToJson() {
        return DataConverter.propertiesToJson(getContent());
    }


    // -------------------------- Provide Information -------------------------- //

    @Override
    protected ActionData createActionData() {
        return super.createActionData()
                .setActionName(JsonAssistantBundle.message("action.properties.to.json.text"))
                .setActionDescription(JsonAssistantBundle.messageOnSystem("action.properties.to.json.description"))
                .setActionIcon(AllIcons.FileTypes.Properties);
    }

    @Override
    protected MessageData createMessageData() {
        return super.createMessageData()
                .setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.properties.to.json"))
                .setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.properties.to.json"));
    }


    /**
     * 判断是否处于Properties文件内
     *
     * @return 如果处于Properties文件内，返回true，否则返回false
     */
    private boolean isPropertiesFileContext() {
        // 判断是否处于Properties内（Properties解析太宽泛了，需加以限制）
        Editor editor = editorData.getEditor();
        Project project = editor.getProject();
        FileType fileType = PlatformUtil.getDocumentFileType(project, editor.getDocument());
        return PlatformUtil.isPropertiesFileType(fileType);
    }

}
