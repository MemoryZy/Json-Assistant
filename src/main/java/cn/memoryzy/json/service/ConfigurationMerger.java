package cn.memoryzy.json.service;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.TreeViewMode;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.service.persistent.state.v2.GeneralState;
import cn.memoryzy.json.service.persistent.state.v2.TreeStructureState;
import cn.memoryzy.json.service.persistent.v2.GeneralSettings;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.ide.impl.convert.JDomConvertingUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import org.jdom.Element;
import org.jetbrains.jps.model.serialization.JDomSerializationUtil;

import java.io.File;
import java.util.Optional;

/**
 * 配置合并
 *
 * @author Memory
 * @since 2025/6/17
 */
@Service(Service.Level.APP)
public final class ConfigurationMerger {

    private final Element settingsElement = getPluginSettingsElement();

    // TODO 初次导入完成后，在原来的xml中添加一个标记，表示已经同步，但是最好是直接删掉xml

    public static ConfigurationMerger getInstance() {
        return ApplicationManager.getApplication().getService(ConfigurationMerger.class);
    }

    public void mergeGeneralLegacySettings() {
        if (null == settingsElement) return;

        String generalState = settingsElement.getAttributeValue("generalState");
        String structureState = settingsElement.getAttributeValue("structureState");

        Optional<TreeStructureState> treeStructureOptional = Optional.ofNullable(GeneralSettings.getInstance().getState())
                .map(GeneralState::getTreeStructureState);

        if (StrUtil.isNotBlank(generalState)) {
            ObjectWrapper objectWrapper = JsonUtil.parseObject(generalState);
            String treeDisplayMode = (String) objectWrapper.get("treeDisplayMode");
            TreeViewMode treeViewMode = TreeViewMode.of(treeDisplayMode);
            if (null != treeViewMode) {
                treeStructureOptional.ifPresent(state -> state.setTreeViewMode(treeViewMode));
            }
        }

        if (StrUtil.isNotBlank(structureState)) {
            ObjectWrapper objectWrapper = JsonUtil.parseObject(generalState);
            Boolean displayNodePath = (Boolean) objectWrapper.get("displayNodePath");
            if (null != displayNodePath) {
                treeStructureOptional.ifPresent(state -> state.setDisplayNodePath(displayNodePath));
            }
        }




    }

    public void mergeToolWindowSettings() {

    }

    private Element getPluginSettingsElement() {
        try {
            File file = PlatformUtil.getOptionsConfigFile("JsonAssistantPersistentState");
            if (file.exists()) {
                Element root = JDomConvertingUtil.load(file.toPath());
                if (null != root) {
                    return JDomSerializationUtil.findComponent(root, "Json Assistant Settings");
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

}
