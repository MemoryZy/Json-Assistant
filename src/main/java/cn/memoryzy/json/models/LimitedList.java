package cn.memoryzy.json.models;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Memory
 * @since 2024/8/10
 */
public class LimitedList<T> extends AbstractList<T> {

    private final List<T> list;
    private final int limit;
    private final ReentrantLock lock;

    public LimitedList(int limit) {
        this.list = new ArrayList<>(limit);
        this.limit = limit;
        this.lock = new ReentrantLock(true);
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
        lock.lock();
        try {
            if (list.size() == limit) {
                list.remove(0);
            }

            return list.add(element);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        lock.lock();
        try {
            boolean added = list.addAll(index, c);
            removeUnnecessaryElement();
            return added;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        lock.lock();
        try {
            boolean added = list.addAll(c);
            removeUnnecessaryElement();
            return added;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean contains(Object element) {
        return list.contains(element);
    }

    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    private void removeUnnecessaryElement() {
        if (list.size() > limit) {
            int removeCount = list.size() - limit;
            // 去除开头的removeCount个元素
            for (int i = 0; i < removeCount; i++) {
                list.remove(0);
            }
        }
    }

}
