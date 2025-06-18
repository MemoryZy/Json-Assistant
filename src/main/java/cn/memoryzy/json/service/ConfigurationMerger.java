package cn.memoryzy.json.service;

import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.ide.impl.convert.JDomConvertingUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.jps.model.serialization.JDomSerializationUtil;

import java.io.File;
import java.util.List;

/**
 * 配置合并
 *
 * @author Memory
 * @since 2025/6/17
 */
@Service(Service.Level.APP)
public final class ConfigurationMerger {

    // private final Element rootElement = getPluginSettingsElement();

    public static ConfigurationMerger getInstance() {
        return ApplicationManager.getApplication().getService(ConfigurationMerger.class);
    }

    public void mergeGeneralLegacySettings() {
        Element root = getPluginSettingsElement();
        final Element settings = JDomSerializationUtil.findComponent(root, "Json Assistant Settings");

        List<Attribute> attributes = settings.getAttributes();

        for (Attribute attribute : attributes) {

            String name = attribute.getName();

            String qualifiedName = attribute.getQualifiedName();

            String value = attribute.getValue();


            System.out.println();

        }

        System.out.println();
    }

    public void mergeToolWindowSettings() {

    }

    private Element getPluginSettingsElement() {
        File file = PlatformUtil.getOptionsConfigFile("JsonAssistantPersistentState");
        if (file.exists()) {
            return JDomConvertingUtil.load(file.toPath());
        }

        return null;
    }

}
