package cn.memoryzy.json.models;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Memory
 * @since 2024/8/10
 */
public class LimitedList<T> extends AbstractList<T> {

    private final List<T> history;
    private final int limit;
    private final ReentrantLock lock;

    public LimitedList(int limit) {
        this.history = new ArrayList<>(limit);
        this.limit = limit;
        this.lock = new ReentrantLock(true);
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
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
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
