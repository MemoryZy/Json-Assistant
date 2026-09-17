package cn.memoryzy.json.service.persistent.state;

import cn.memoryzy.json.service.persistent.HistoryManager;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.List;

/**
 * 专门负责承载正反序列化数据
 *
 * @author Memory
 * @since 2025/12/15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordSerialization {

    /**
     * 最后存储时间
     */
    private Date lastSaved;

    /**
     * 历史记录信息
     */
    private List<JsonRecord> records;

    /**
     * 历史记录分组信息
     */
    private List<JsonGroup> groups;

    public RecordSerialization() {

    }

    public RecordSerialization(HistoryManager manager) {
        this.lastSaved = manager.getLastSaved();
        this.records = manager.getRecords();
        this.groups = manager.getGroups();
    }


    public Date getLastSaved() {
        return lastSaved;
    }

    public void setLastSaved(Date lastSaved) {
        this.lastSaved = lastSaved;
    }

    public List<JsonRecord> getRecords() {
        return records;
    }

    public void setRecords(List<JsonRecord> records) {
        this.records = records;
    }

    public List<JsonGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<JsonGroup> groups) {
        this.groups = groups;
    }
}
