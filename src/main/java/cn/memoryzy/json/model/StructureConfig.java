package cn.memoryzy.json.model;

/**
 * @author Memory
 * @since 2025/2/19
 */
public class StructureConfig {

    private boolean needBorder;

    private boolean needToolbar;

    public StructureConfig(boolean needBorder, boolean needToolbar) {
        this.needBorder = needBorder;
        this.needToolbar = needToolbar;
    }

    public static StructureConfig of() {
        return new StructureConfig(true, true);
    }

    public static StructureConfig of(boolean needBorder) {
        return new StructureConfig(needBorder, true);
    }

    public static StructureConfig of(boolean needBorder, boolean needToolbar) {
        return new StructureConfig(needBorder, needToolbar);
    }

    public boolean isNeedBorder() {
        return needBorder;
    }

    public void setNeedBorder(boolean needBorder) {
        this.needBorder = needBorder;
    }

    public boolean isNeedToolbar() {
        return needToolbar;
    }

    public void setNeedToolbar(boolean needToolbar) {
        this.needToolbar = needToolbar;
    }
}
