package cn.memoryzy.json.model;

import cn.memoryzy.json.util.JsonUtil;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/10
 */
public class LimitedList extends AbstractList<String> {

    private final List<String> history;
    private final int limit;

    public LimitedList(int limit) {
        this.history = new ArrayList<>(limit);
        this.limit = limit;
    }

    @Override
    public String get(int index) {
        return history.get(index);
    }

    @Override
    public int size() {
        return history.size();
    }

    @Override
    public boolean add(String element) {
        if (!isContains(element)) {
            history.add(0, element);
            if (history.size() > limit) {
                history.remove(history.size() - 1);
            }
        } else {
            String first = history.get(0);
            if (!deserializeThenCompare(first, element)) {
                deserializeCompareAndRemove(element);
                history.add(0, element);
            }
        }

        // TODO 要加添加时间的，直接在末尾加上特定的标志，从中提取时间。

        return true;
    }

    @Override
    public boolean contains(Object element) {
        return history.contains(element);
    }

    @Override
    public String remove(int index) {
        return history.remove(index);
    }

    private boolean isContains(String element) {
        Object addObject = JsonUtil.toBean(element);
        for (String oriElement : history) {
            Object oriObject = JsonUtil.toBean(oriElement);
            if (Objects.equals(addObject, oriObject)) {
                return true;
            }
        }

        return false;
    }

    private boolean deserializeThenCompare(String jsonStr1, String jsonStr2) {
        Object object1 = JsonUtil.toBean(jsonStr1);
        Object object2 = JsonUtil.toBean(jsonStr2);
        return Objects.equals(object1, object2);
    }

    private void deserializeCompareAndRemove(String element) {
        Object addObject = JsonUtil.toBean(element);
        for (int i = history.size() - 1; i >= 0; i--) {
            Object oriObject = JsonUtil.toBean(history.get(i));
            if (Objects.equals(addObject, oriObject)) {
                history.remove(i);
                break;
            }
        }
    }

}
