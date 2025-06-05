package cn.memoryzy.json.model.structure;

/**
 * @author Memory
 * @since 2025/6/5
 */
class ViewConfiguration {

    /**
     * 是否需要边框
     */
    private boolean needBorder;

    /**
     * 是否需要工具栏
     */
    private boolean needToolbar;

    /**
     * 自动展开层级
     */
    private int expandLevel = 2;

    /**
     * 是否需要刷新按钮
     */
    private boolean needRefresh;


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

    public int getExpandLevel() {
        return expandLevel;
    }

    public void setExpandLevel(int expandLevel) {
        this.expandLevel = expandLevel;
    }

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        this.needRefresh = needRefresh;
    }
}
