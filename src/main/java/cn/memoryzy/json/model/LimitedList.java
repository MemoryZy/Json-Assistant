package cn.memoryzy.json.model;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/10
 */
public class LimitedList<T> extends AbstractList<T> {

    private final List<T> history;
    private final int limit;

    public LimitedList(int limit) {
        this.history = new ArrayList<>(limit);
        this.limit = limit;
    }

    @Override
    public T get(int index) {
        return history.get(index);
    }

    @Override
    public int size() {
        return history.size();
    }

    @Override
    public boolean add(T element) {
        if (!history.contains(element)) {
            history.add(0, element);
            if (history.size() > limit) {
                history.remove(history.size() - 1);
            }
        } else {
            T first = history.get(0);
            if (!Objects.equals(first, element)) {
                history.remove(element);
                history.add(0, element);
            }
        }

        return true;
    }

    @Override
    public boolean contains(Object element) {
        return history.contains(element);
    }

    @Override
    public T remove(int index) {
        return history.remove(index);
    }

}
