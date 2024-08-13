package cn.memoryzy.json.models;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/8/10
 */
public class LimitedList<T> extends AbstractList<T> {

    private final List<T> list;
    private final int limit;

    public LimitedList(int limit) {
        this.list = new ArrayList<>(limit);
        this.limit = limit;
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean add(T element) {
        if (list.size() == limit) {
            list.remove(0);
        }

        return list.add(element);
    }

    @Override
    public boolean contains(Object element) {
        return list.contains(element);
    }
}
