package cn.memoryzy.json.model.sort;

/**
* @author Memory
* @since 2025/9/1
*/
public class KeyValuePair {

    private final String key;
    private final Object value;

    public KeyValuePair(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
