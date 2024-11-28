package cn.memoryzy.json.model;

import cn.memoryzy.json.model.wrapper.JsonWrapper;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.ArrayUtils;

import java.util.LinkedList;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/11/25
 */
public class HistoryLimitedList extends LinkedList<HistoryEntry> {
    private final int limit;

    public HistoryLimitedList(int limit) {
        super();
        this.limit = limit;
    }

    public HistoryEntry add(Project project, JsonWrapper jsonWrapper) {
        HistoryEntry historyEntry = new HistoryEntry(HistoryEntry.calculateId(project), jsonWrapper);
        // 判断 Json 记录是否已经存在于记录内
        if (!exists(jsonWrapper)) {
            // 不存在，则将其添加到首位（add方法会判断容量是否超出而去除最老节点）
            add(0, historyEntry);
        } else {
            // 存在，则判断是否需要移动位置
            HistoryEntry first = getFirst();
            // 判断其是否为第0位的元素，如果是，则不管，否则将其移动至首位
            if (!Objects.equals(first.getJsonWrapper(), jsonWrapper)) {
                // 移除该元素
                removeIf(el -> Objects.equals(el.getJsonWrapper(), jsonWrapper));
                // 再添加到首位
                add(0, historyEntry);
            }
        }

        return historyEntry;
    }

    @Override
    public boolean add(HistoryEntry element) {
        boolean added = super.add(element);
        if (size() > limit) {
            // 移除最老的元素
            super.removeLast();
        }

        return added;
    }

    @Override
    public void add(int index, HistoryEntry element) {
        super.add(index, element);
        if (size() > limit) {
            // 移除最老的元素
            super.removeLast();
        }
    }

    @Override
    public boolean addAll(int index, java.util.Collection<? extends HistoryEntry> c) {
        boolean added = super.addAll(index, c);
        while (size() > limit) {
            // 移除最老的元素
            super.removeLast();
        }

        return added;
    }

    @Override
    public boolean addAll(java.util.Collection<? extends HistoryEntry> c) {
        boolean added = super.addAll(c);
        while (size() > limit) {
            // 移除最老的元素
            super.removeLast();
        }
        return added;
    }

    public void removeById(Integer... idArray) {
        removeIf(el -> ArrayUtils.contains(idArray, el.getId()));
    }

    /**
     * 判断是否已经存在，存在则返回true，不存在则返回false
     *
     * @param addElement 要添加的元素
     */
    public boolean exists(JsonWrapper addElement) {
        return this.stream().anyMatch(el -> Objects.equals(addElement, el.getJsonWrapper()));
    }


}
