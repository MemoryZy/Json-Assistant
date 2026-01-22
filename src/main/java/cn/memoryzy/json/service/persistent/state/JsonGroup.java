package cn.memoryzy.json.service.persistent.state;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Memory
 * @since 2025/12/15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonGroup extends BaseData {

    /**
     * 包含的记录ID
     */
    private Set<String> recordIds = new HashSet<>();


    public void add(String recordId) {
        recordIds.add(recordId);
    }

    public void addAll(Collection<String> recordIds) {
        this.recordIds.addAll(recordIds);
    }

    public void remove(String recordId) {
        recordIds.remove(recordId);
    }

    public Set<String> getRecordIds() {
        return recordIds;
    }

    public void setRecordIds(Set<String> recordIds) {
        this.recordIds = recordIds;
    }

}
