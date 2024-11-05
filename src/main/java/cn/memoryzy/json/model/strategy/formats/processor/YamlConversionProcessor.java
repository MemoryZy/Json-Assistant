package cn.memoryzy.json.model.strategy.formats.processor;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.YamlDocEntry;
import cn.memoryzy.json.model.data.ActionData;
import cn.memoryzy.json.model.data.EditorData;
import cn.memoryzy.json.model.data.MessageData;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.ui.dialog.MultiYamlDocumentChooser;
import cn.memoryzy.json.util.YamlUtil;
import com.intellij.icons.AllIcons;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2024/11/3
 */
public class YamlConversionProcessor extends AbstractGlobalTextConversionProcessor {

    public YamlConversionProcessor(EditorData editorData) {
        super(editorData, true);
    }

    @Override
    public boolean canConvert(String text) {
        return YamlUtil.isYaml(text);
    }

    @Override
    public String convertToJson() {
        return YamlUtil.toJson(getContent());
    }

    @Override
    public void preprocessing() {
        // Yaml文本中可能存在多份文档，但不能同时转换多份文档
        String content = getContent();
        boolean multipleYamlDocuments = YamlUtil.containsMultipleYamlDocuments(content);
        if (multipleYamlDocuments) {
            List<Object> values = YamlUtil.loadAll(content);
            List<Object> matchingValues = values.stream().filter(el -> el instanceof List || el instanceof Map).collect(Collectors.toList());
            // 若有效文档只有一个，则默认将其作转换的文档（指那些加了文档分割线，但是未写内容）
            if (matchingValues.size() == 1) {
                Object obj = matchingValues.get(0);
                // 设置值（将对象转为 Yaml，不影响 convertToJson 方法运行）
                setContent(YamlUtil.toYaml(obj));
            } else {
                // 多文档选择
                MultiYamlDocumentChooser chooser = new MultiYamlDocumentChooser(matchingValues);
                if (chooser.showAndGet()) {
                    YamlDocEntry selectValue = chooser.getSelectValue();
                    setContent(YamlUtil.toYaml(selectValue.getValue()));
                }
            }
        }
    }


    // -------------------------- Provide Information -------------------------- //

    @Override
    protected ActionData createActionData() {
        return super.createActionData()
                .setActionName(JsonAssistantBundle.message("action.yaml.to.json.text"))
                .setActionDescription(JsonAssistantBundle.messageOnSystem("action.yaml.to.json.description"))
                .setActionIcon(AllIcons.FileTypes.Yaml);
    }

    @Override
    protected MessageData createMessageData() {
        return super.createMessageData()
                .setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.yaml.to.json.text"))
                .setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.yaml.to.json.text"));
    }


}
