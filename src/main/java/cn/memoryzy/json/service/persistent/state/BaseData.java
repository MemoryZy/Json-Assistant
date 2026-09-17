package cn.memoryzy.json.service.persistent.state;

import java.util.Date;

/**
 * @author Memory
 * @since 2025/11/25
 */
public abstract class BaseData {

    /**
     * 唯一标识符
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 父级ID
     */
    private String parentId;

    /**
     * 是否删除的标记（true-已删除，false-未删除）
     */
    private Boolean isDeleted;

    /**
     * 删除时间
     */
    private Date deletedTime;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 最后更新时间
     */
    private Date updatedTime;


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getParentId() {
        return parentId;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public Date getDeletedTime() {
        return deletedTime;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public void setDeletedTime(Date deletedTime) {
        this.deletedTime = deletedTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}
