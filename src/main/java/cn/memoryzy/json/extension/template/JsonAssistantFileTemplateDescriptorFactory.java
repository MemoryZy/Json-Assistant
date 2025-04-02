package cn.memoryzy.json.extension.template;

import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.constant.PluginConstant;
import com.intellij.icons.AllIcons;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;

/**
 * @author Memory
 * @since 2025/4/2
 */
public class JsonAssistantFileTemplateDescriptorFactory implements FileTemplateGroupDescriptorFactory {

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(JsonAssistantPlugin.PLUGIN_NAME, AllIcons.FileTypes.Custom);
        group.addTemplate(new FileTemplateDescriptor(PluginConstant.NEW_CLASS_TEMPLATE_NAME, AllIcons.FileTypes.Java));
        group.addTemplate(new FileTemplateDescriptor(PluginConstant.INNER_CLASS_TEMPLATE_NAME, AllIcons.FileTypes.Java));
        return group;
    }

}
