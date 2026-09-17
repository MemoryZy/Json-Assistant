package cn.memoryzy.json.ui.tree;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intellij.util.xmlb.annotations.Transient;

/**
 * 基础树节点
 *
 * @author Memory
 * @since 2025/9/3
 */
public abstract class BaseNode {

    /**
     * 是否匹配成功（主要是为了解决中文检索乱码问题）
     */
    private boolean matched;

    @Transient
    @JsonIgnore
    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }
}
