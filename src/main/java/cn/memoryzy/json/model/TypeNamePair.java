package cn.memoryzy.json.model;

/**
 * @author Memory
 * @since 2025/5/19
 */
public class TypeNamePair {

    private final String type;
    private final String name;

    public TypeNamePair(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
