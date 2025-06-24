package cn.memoryzy.json.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.*;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.service.persistent.state.v2.*;
import cn.memoryzy.json.service.persistent.v2.*;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.conversion.ComponentManagerSettings;
import com.intellij.ide.impl.convert.JDomConvertingUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.jps.model.serialization.JDomSerializationUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    private final Element blacklistPersistentDataElement = getBlacklistPersistentDataElement();

    public static ConfigurationMerger getInstance() {
        return ApplicationManager.getApplication().getService(ConfigurationMerger.class);
    }

    public void mergeGlobalLegacySettings() {
        if (null == settingsElement) return;

        try {
            mergeGeneralLegacySettings();
            mergeToolWindowLegacySettings();
            mergeSerializationLegacySettings();
            mergeBlacklistLegacyData();
            // 删除旧配置文件
//            deleteLegacyPluginSettingsFile();
        } catch (Exception e) {
            LOG.warn("[Json Assistant] An exception occurred when merging the old configuration", e);
        }
    }

    public void mergeProjectLegacySettings(Project project) {
        try {
            mergeHistoryLegacySettings(project);
        } catch (Exception e) {
            LOG.warn("[Json Assistant] An exception occurred when merging the old project configuration", e);
        }
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    private void mergeGeneralLegacySettings() {
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
                    if (null != displayCount) announcementStats.setDisplayCount(displayCount);
                    if (null != lastShownTime) announcementStats.setLastShownTime(lastShownTime);
                    if (null != shouldShowAgain) announcementStats.setShouldShowAgain(shouldShowAgain);

                    readAnnouncements.put(entry.getKey(), announcementStats);
                }
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void mergeToolWindowLegacySettings() {
        ToolWindowSettings settings = ToolWindowSettings.getInstance();
        // ---------------------------------- 编辑器外观
        EditorVisualState visualState = settings.getVisualState();
        String editorAppearanceStateStr = settingsElement.getAttributeValue("editorAppearanceState");
        if (StrUtil.isNotBlank(editorAppearanceStateStr)) {
            ObjectWrapper wrapper = JsonUtil.parseObject(editorAppearanceStateStr);
            Boolean displayLineNumbers = (Boolean) wrapper.get("displayLineNumbers");
            Boolean foldingOutline = (Boolean) wrapper.get("foldingOutline");
            String colorSchemeStr = (String) wrapper.get("colorScheme");
            ColorScheme colorScheme = ColorScheme.of(colorSchemeStr);

            if (null != displayLineNumbers) visualState.setShowLineNumbers(displayLineNumbers);
            if (null != foldingOutline) visualState.setShowFoldingOutline(foldingOutline);
            if (null != colorScheme) visualState.setColorScheme(colorScheme);
        }

        // ---------------------------------- 编辑器行为
        EditorBehaviorState behaviorState = settings.getBehaviorState();
        String editorBehaviorStateStr = settingsElement.getAttributeValue("editorBehaviorState");
        if (StrUtil.isNotBlank(editorBehaviorStateStr)) {
            ObjectWrapper wrapper = JsonUtil.parseObject(editorBehaviorStateStr);
            Boolean recognizeOtherFormats = (Boolean) wrapper.get("recognizeOtherFormats");
            Boolean recognizeXmlFormat = (Boolean) wrapper.get("recognizeXmlFormat");
            Boolean recognizeYamlFormat = (Boolean) wrapper.get("recognizeYamlFormat");
            Boolean recognizeTomlFormat = (Boolean) wrapper.get("recognizeTomlFormat");
            Boolean recognizeUrlParamFormat = (Boolean) wrapper.get("recognizeUrlParamFormat");
            Boolean promptBeforeImport = (Boolean) wrapper.get("promptBeforeImport");

            if (null != recognizeOtherFormats) behaviorState.setAutoRecognizeFormats(recognizeOtherFormats);

            Set<DataFormatType> enabledFormats = new HashSet<>();
            if (null != recognizeXmlFormat && recognizeXmlFormat) enabledFormats.add(DataFormatType.XML);
            if (null != recognizeYamlFormat && recognizeYamlFormat) enabledFormats.add(DataFormatType.YAML);
            if (null != recognizeTomlFormat && recognizeTomlFormat) enabledFormats.add(DataFormatType.TOML);
            if (null != recognizeUrlParamFormat && recognizeUrlParamFormat)
                enabledFormats.add(DataFormatType.URL_PARAM);

            if (CollUtil.isNotEmpty(enabledFormats)) behaviorState.setEnabledFormats(enabledFormats);
            if (null != promptBeforeImport) behaviorState.setShouldPromptBeforeImport(promptBeforeImport);
        }

        // ---------------------------------- JSON 查询配置
        QueryState queryState = settings.getQueryState();
        String queryStateStr = settingsElement.getAttributeValue("queryState");
        if (StrUtil.isNotBlank(queryStateStr)) {
            ObjectWrapper wrapper = JsonUtil.parseObject(queryStateStr);
            String querySchemaStr = (String) wrapper.get("querySchema");
            JsonQueryLanguage queryLanguage = JsonQueryLanguage.of(querySchemaStr);
            Boolean showOriginalText = (Boolean) wrapper.get("showOriginalText");

            if (null != queryLanguage) queryState.setQueryLanguage(queryLanguage);
            if (null != showOriginalText) queryState.setDisplayOriginalText(showOriginalText);
        }

        HistoryState historyState = settings.getHistoryState();
        String historyStateStr = settingsElement.getAttributeValue("historyState");
        if (StrUtil.isNotBlank(historyStateStr)) {
            ObjectWrapper wrapper = JsonUtil.parseObject(historyStateStr);
            Boolean switchHistory = (Boolean) wrapper.get("switchHistory");
            String historyViewTypeStr = (String) wrapper.get("historyViewType");
            HistoryDisplayMode historyViewType = HistoryDisplayMode.of(historyViewTypeStr);
            Boolean autoStore = (Boolean) wrapper.get("autoStore");

            if (null != switchHistory) historyState.setEnableHistory(switchHistory);
            if (null != historyViewType) historyState.setHistoryDisplayMode(historyViewType);
            if (null != autoStore) historyState.setAutoRecordHistory(autoStore);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void mergeSerializationLegacySettings() {
        SerializationSettings settings = SerializationSettings.getInstance();
        // ---------------------------------- 序列化
        SerializationState serializationState = settings.getSerializationState();
        String attributeSerializationStateStr = settingsElement.getAttributeValue("attributeSerializationState");
        if (StrUtil.isNotBlank(attributeSerializationStateStr)) {
            ObjectWrapper wrapper = JsonUtil.parseObject(attributeSerializationStateStr);
            Boolean includeRandomValues = (Boolean) wrapper.get("includeRandomValues");
            Boolean recognitionFastJsonAnnotation = (Boolean) wrapper.get("recognitionFastJsonAnnotation");
            Boolean recognitionJacksonAnnotation = (Boolean) wrapper.get("recognitionJacksonAnnotation");

            if (null != includeRandomValues) serializationState.setSerializeRandomValues(includeRandomValues);
            if (null != recognitionFastJsonAnnotation)
                serializationState.setDetectFastJsonAnnotations(recognitionFastJsonAnnotation);
            if (null != recognitionJacksonAnnotation)
                serializationState.setDetectJacksonAnnotations(recognitionJacksonAnnotation);
        }

        // ---------------------------------- 反序列化
        DeserializationState deserializationState = settings.getDeserializationState();
        String deserializerStateStr = settingsElement.getAttributeValue("deserializerState");
        if (StrUtil.isNotBlank(deserializerStateStr)) {
            ObjectWrapper wrapper = JsonUtil.parseObject(deserializerStateStr);
            Boolean fastJsonAnnotation = (Boolean) wrapper.get("fastJsonAnnotation");
            Boolean fastJson2Annotation = (Boolean) wrapper.get("fastJson2Annotation");
            Boolean jacksonAnnotation = (Boolean) wrapper.get("jacksonAnnotation");
            Boolean keepCamelCase = (Boolean) wrapper.get("keepCamelCase");
            Boolean dataLombokAnnotation = (Boolean) wrapper.get("dataLombokAnnotation");
            Boolean accessorsChainLombokAnnotation = (Boolean) wrapper.get("accessorsChainLombokAnnotation");
            Boolean getterLombokAnnotation = (Boolean) wrapper.get("getterLombokAnnotation");
            Boolean setterLombokAnnotation = (Boolean) wrapper.get("setterLombokAnnotation");
            Boolean swaggerAnnotation = (Boolean) wrapper.get("swaggerAnnotation");
            Boolean swaggerV3Annotation = (Boolean) wrapper.get("swaggerV3Annotation");

            if (null != fastJsonAnnotation) deserializationState.setEnableFastJsonAnnotation(fastJsonAnnotation);
            if (null != fastJson2Annotation) deserializationState.setEnableFastJson2Annotation(fastJson2Annotation);
            if (null != jacksonAnnotation) deserializationState.setEnableJacksonAnnotation(jacksonAnnotation);
            if (null != keepCamelCase) deserializationState.setKeepFieldCamelCase(keepCamelCase);
            if (null != dataLombokAnnotation) deserializationState.setEnableLombokData(dataLombokAnnotation);
            if (null != accessorsChainLombokAnnotation)
                deserializationState.setEnableLombokChainAccessors(accessorsChainLombokAnnotation);
            if (null != getterLombokAnnotation) deserializationState.setEnableLombokGetter(getterLombokAnnotation);
            if (null != setterLombokAnnotation) deserializationState.setEnableLombokSetter(setterLombokAnnotation);
            if (null != swaggerAnnotation) deserializationState.setEnableSwaggerAnnotation(swaggerAnnotation);
            if (null != swaggerV3Annotation) deserializationState.setEnableSwagger3Annotation(swaggerV3Annotation);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void mergeBlacklistLegacyData() {
        if (null == blacklistPersistentDataElement) return;
        BlacklistManager manager = BlacklistManager.getInstance();
        String blacklistStr = blacklistPersistentDataElement.getAttributeValue("blacklist");
        if (StrUtil.isNotBlank(blacklistStr)) {
            blacklistStr = StrUtil.str(Base64.decode(blacklistStr), StandardCharsets.UTF_8);
            ArrayWrapper arrayWrapper = JsonUtil.isJson(blacklistStr)
                    ? JsonUtil.parseArray(blacklistStr)
                    : Json5Util.parseArray(blacklistStr);

            if (arrayWrapper.isEmpty()) return;

            for (Object el : arrayWrapper) {
                ObjectWrapper element = (ObjectWrapper) el;
                Integer id = (Integer) element.get("id");
                String name = (String) element.get("name");
                String shortText = (String) element.get("shortText");
                String originalText = (String) element.get("originalText");
                String originalDataTypeStr = (String) element.get("originalDataType");
                DataFormatType originalDataType = DataFormatType.ofValue(originalDataTypeStr);
                JsonWrapper jsonWrapper = (JsonWrapper) element.get("jsonWrapper");
                Object insertTimeTmp = element.get("insertTime");

                // 若id和原文都不存在
                if (null == id || StrUtil.isBlank(originalText)) {
                    continue;
                }

                JsonRecord record = new JsonRecord()
                        .setId(id).setRawText(originalText);

                if (null != name) record.setName(name);
                if (null != shortText) record.setDisplayText(shortText);
                if (null != originalDataType) record.setSourceType(originalDataType);
                if (null != jsonWrapper) record.setWrapper(jsonWrapper);

                Long insertTime = null;
                if (null != insertTimeTmp) {
                    // 可能是时间戳、可能是时间字符串
                    String timeStr = insertTimeTmp + "";
                    if (!JsonAssistantUtil.isValidTimestamp(timeStr)) {
                        try {
                            insertTime = DateUtil.parse(timeStr).getTime();
                        } catch (Exception ignored) {
                        }
                    } else {
                        insertTime = NumberUtil.parseLong(timeStr);
                    }
                }

                // 如果没有时间，则给一个默认值
                if (null == insertTime) {
                    insertTime = System.currentTimeMillis();
                }

                record.setCreateTime(insertTime).setUpdateTime(insertTime);

                manager.add(record);
            }
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void mergeHistoryLegacySettings(Project project) {
        Element dataElement = getHistoryPersistentDataElement(project);
        if (null == dataElement) return;

        HistoryManager manager = HistoryManager.getInstance(project);
        String historyStr = dataElement.getAttributeValue("history");
        if (StrUtil.isNotBlank(historyStr)) {
            // TODO 看看要不要兼容更早期的版本
            if (Base64.isBase64(historyStr)) {
                historyStr = StrUtil.str(Base64.decode(historyStr), StandardCharsets.UTF_8);
            }

            ArrayWrapper arrayWrapper = JsonUtil.isJson(historyStr)
                    ? JsonUtil.parseArray(historyStr)
                    : Json5Util.parseArray(historyStr);

            if (arrayWrapper.isEmpty()) return;

            for (Object el : arrayWrapper) {
                ObjectWrapper element = (ObjectWrapper) el;
                Integer id = (Integer) element.get("id");
                String name = (String) element.get("name");
                String shortText = (String) element.get("shortText");
                String jsonString = (String) element.get("jsonString");
                JsonWrapper jsonWrapper = (JsonWrapper) element.get("jsonWrapper");
                Object insertTimeTmp = element.get("insertTime");

                // 若id和原文都不存在
                if (null == id || StrUtil.isBlank(jsonString)) {
                    continue;
                }

                JsonRecord record = new JsonRecord()
                        .setId(id)
                        .setRawText(jsonString)
                        .setSourceType(JsonUtil.isJson(jsonString) ? DataFormatType.JSON : DataFormatType.JSON5);

                if (null != name) record.setName(name);
                if (null != shortText) record.setDisplayText(shortText);
                if (null != jsonWrapper) record.setWrapper(jsonWrapper);

                Long insertTime = null;
                if (null != insertTimeTmp) {
                    // 可能是时间戳、可能是时间字符串
                    String timeStr = insertTimeTmp + "";
                    if (!JsonAssistantUtil.isValidTimestamp(timeStr)) {
                        try {
                            insertTime = DateUtil.parse(timeStr).getTime();
                        } catch (Exception ignored) {
                        }
                    } else {
                        insertTime = NumberUtil.parseLong(timeStr);
                    }
                }

                // 如果没有时间，则给一个默认值
                if (null == insertTime) {
                    insertTime = System.currentTimeMillis();
                }

                record.setCreateTime(insertTime).setUpdateTime(insertTime);
                manager.addEntry(record);
            }
        }

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

    private void deleteLegacyPluginSettingsFile() {
        File file = PlatformUtil.getOptionsConfigFile("JsonAssistantPersistentState");
        FileUtil.del(file);
    }

    private Element getBlacklistPersistentDataElement() {
        try {
            File file = PlatformUtil.getOptionsConfigFile("ClipboardDataBlacklistPersistentState");
            if (file.exists()) {
                Element root = JDomConvertingUtil.load(file.toPath());
                if (null != root) {
                    return JDomSerializationUtil.findComponent(root, "Json Assistant Clipboard Data Blacklist");
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private void deleteLegacyBlacklistPersistentFile() {
        File file = PlatformUtil.getOptionsConfigFile("ClipboardDataBlacklistPersistentState");
        FileUtil.del(file);
    }

    private Element getHistoryPersistentDataElement(Project project) {
        try {
            ComponentManagerSettings projectSettings = PlatformUtil.createProjectSettings(project, "JsonAssistantHistoryState.xml");
            return projectSettings.getComponentElement("Json Assistant History");

        } catch (Exception ignored) {
        }

        return null;
    }

    private void deleteLegacyHistoryPersistentFile(Project project) {
        ComponentManagerSettings projectSettings = PlatformUtil.createProjectSettings(project, "JsonAssistantHistoryState.xml");
        Path path = projectSettings.getPath();
        FileUtil.del(path);
    }

}
