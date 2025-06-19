package cn.memoryzy.json.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.TreeViewMode;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.service.persistent.state.v2.*;
import cn.memoryzy.json.service.persistent.v2.GeneralSettings;
import cn.memoryzy.json.service.persistent.v2.ToolWindowSettings;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.ide.impl.convert.JDomConvertingUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import org.jdom.Element;
import org.jetbrains.jps.model.serialization.JDomSerializationUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * 配置合并
 *
 * @author Memory
 * @since 2025/6/17
 */
@Service(Service.Level.APP)
public final class ConfigurationMerger {

    private static final Logger LOG = Logger.getInstance(ConfigurationMerger.class);

    private final Element settingsElement = getPluginSettingsElement();

    // TODO 初次导入完成后，在原来的xml中添加一个标记，表示已经同步，但是最好是直接删掉xml

    public static ConfigurationMerger getInstance() {
        return ApplicationManager.getApplication().getService(ConfigurationMerger.class);
    }

    public void mergeLegacySettings() {
        if (null == settingsElement) return;

        try {
            mergeGeneralLegacySettings();


        } catch (Exception e) {
            LOG.warn("An exception occurred when merging the old configuration", e);
        }
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    public void mergeGeneralLegacySettings() {
        // ------------------------------- 树配置
        String generalState = settingsElement.getAttributeValue("generalState");
        String structureState = settingsElement.getAttributeValue("structureState");

        Optional<GeneralState> optional = Optional.ofNullable(GeneralSettings.getInstance().getState());
        Optional<TreeStructureState> treeStructureOptional = optional.map(GeneralState::getTreeStructureState);
        Optional<Map<String, AnnouncementStats>> readAnnouncementsOptional = optional.map(GeneralState::getReadAnnouncements);

        // 树展示模式
        if (StrUtil.isNotBlank(generalState)) {
            ObjectWrapper objectWrapper = JsonUtil.parseObject(generalState);
            String treeDisplayMode = (String) objectWrapper.get("treeDisplayMode");
            TreeViewMode treeViewMode = TreeViewMode.of(treeDisplayMode);
            if (null != treeViewMode) {
                treeStructureOptional.ifPresent(state -> state.setTreeViewMode(treeViewMode));
            }
        }

        // 树节点展示与否
        if (StrUtil.isNotBlank(structureState)) {
            ObjectWrapper objectWrapper = JsonUtil.parseObject(generalState);
            Boolean displayNodePath = (Boolean) objectWrapper.get("displayNodePath");
            if (null != displayNodePath) {
                treeStructureOptional.ifPresent(state -> state.setDisplayNodePath(displayNodePath));
            }
        }

        // ------------------------------- 公告
        String announcementStatsMapStr = settingsElement.getAttributeValue("announcementStatsMap");
        if (StrUtil.isNotBlank(announcementStatsMapStr)) {
            announcementStatsMapStr = StrUtil.str(Base64.decode(announcementStatsMapStr), StandardCharsets.UTF_8);
            ObjectWrapper announcementStatsMap = JsonUtil.parseObject(announcementStatsMapStr);
            Map<String, AnnouncementStats> readAnnouncements = readAnnouncementsOptional.orElse(null);

            if (MapUtil.isNotEmpty(announcementStatsMap) && null != readAnnouncements) {
                for (Map.Entry<String, Object> entry : announcementStatsMap.entrySet()) {
                    Map<String, Object> value = (Map<String, Object>) entry.getValue();

                    Integer displayCount = (Integer) value.get("displayCount");
                    Long lastShownTime = (Long) value.get("lastShownTime");
                    Boolean shouldShowAgain = (Boolean) value.get("shouldShowAgain");

                    AnnouncementStats announcementStats = new AnnouncementStats();
                    if (null != displayCount) {
                        announcementStats.setDisplayCount(displayCount);
                    }

                    if (null != lastShownTime) {
                        announcementStats.setLastShownTime(lastShownTime);
                    }

                    if (null != shouldShowAgain) {
                        announcementStats.setShouldShowAgain(shouldShowAgain);
                    }

                    readAnnouncements.put(entry.getKey(), announcementStats);
                }
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public void mergeToolWindowLegacySettings() {
        String editorAppearanceStateStr = settingsElement.getAttributeValue("editorAppearanceState");
        String editorBehaviorStateStr = settingsElement.getAttributeValue("editorBehaviorState");
        String queryStateStr = settingsElement.getAttributeValue("queryState");
        String historyStateStr = settingsElement.getAttributeValue("historyState");

        ToolWindowSettings settings = ToolWindowSettings.getInstance();
        EditorVisualState visualState = settings.getVisualState();
        EditorBehaviorState behaviorState = settings.getBehaviorState();
        QueryState queryState = settings.getQueryState();
        HistoryState historyState = settings.getHistoryState();



    }

    public void mergeSerializationLegacySettings() {

    }

    public void mergeBlacklistLegacySettings() {

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
