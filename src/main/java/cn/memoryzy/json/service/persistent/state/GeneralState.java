package cn.memoryzy.json.service.persistent.state;

import cn.memoryzy.json.JsonAssistantPlugin;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.MapAnnotation;

import java.util.HashMap;
import java.util.Map;

/**
 * 常规设置项
 *
 * @author Memory
 * @since 2024/12/11
 */
public class GeneralState {

    /**
     * 配置版本
     */
    private Integer version = JsonAssistantPlugin.CONFIG_VERSION;

    /**
     * 树结构配置项
     */
    private TreeStructureState treeStructureState = new TreeStructureState();

    /**
     * 已读公告的阅读统计信息（key: 公告ID, value: 统计详情）
     */
    private Map<String, AnnouncementStats> readAnnouncements = new HashMap<>();


    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setTreeStructureState(TreeStructureState treeStructureState) {
        this.treeStructureState = treeStructureState;
    }

    public void setReadAnnouncements(Map<String, AnnouncementStats> readAnnouncements) {
        this.readAnnouncements = readAnnouncements;
    }

    @Attribute
    public Integer getVersion() {
        return version;
    }

    public TreeStructureState getTreeStructureState() {
        return treeStructureState;
    }

    @MapAnnotation(surroundWithTag = false)
    public Map<String, AnnouncementStats> getReadAnnouncements() {
        return readAnnouncements;
    }
}