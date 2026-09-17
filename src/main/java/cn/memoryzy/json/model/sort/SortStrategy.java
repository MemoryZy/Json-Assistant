package cn.memoryzy.json.model.sort;

import java.util.List;

/**
 * @author Memory
 * @since 2025/9/1
 */
public interface SortStrategy {

    List<KeyValuePair> sortPairs(List<KeyValuePair> pairs);

}