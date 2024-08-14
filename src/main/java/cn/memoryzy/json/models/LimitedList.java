package cn.memoryzy.json.models;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
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
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean added = list.addAll(index, c);
        removeUnnecessaryElement();
        return added;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        boolean added = list.addAll(c);
        removeUnnecessaryElement();
        return added;
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

            // 或者，如果你想要更高的效率，并且不介意改变元素的顺序，
            // 你可以保留列表的尾部maxSize个元素，丢弃前面的元素
            // list = list.subList(list.size() - maxSize, list.size());
            // 但请注意，这将改变list的引用，可能不是你想要的行为（特别是如果外部还持有list的引用）
        }
    }

}
