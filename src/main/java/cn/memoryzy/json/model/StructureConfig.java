package cn.memoryzy.json.model;

import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Memory
 * @since 2025/2/19
 */
public class StructureConfig {

    private boolean needBorder;

    private boolean needToolbar;

    private int expandLevel = 2;

    private boolean needRefresh;

    private VirtualFile file;

    public StructureConfig(boolean needBorder, boolean needToolbar) {
        this.needBorder = needBorder;
        this.needToolbar = needToolbar;
    }

    public StructureConfig(boolean needBorder, boolean needToolbar, int expandLevel) {
        this.needBorder = needBorder;
        this.needToolbar = needToolbar;
        this.expandLevel = expandLevel;
    }

    public StructureConfig(boolean needBorder, boolean needToolbar, int expandLevel, boolean needRefresh) {
        this.needBorder = needBorder;
        this.needToolbar = needToolbar;
        this.expandLevel = expandLevel;
        this.needRefresh = needRefresh;
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

    public static StructureConfig of(boolean needBorder, int expandLevel) {
        return new StructureConfig(needBorder, true, expandLevel);
    }

    public static StructureConfig of(boolean needBorder, int expandLevel, boolean needRefresh, VirtualFile file) {
        StructureConfig structureConfig = new StructureConfig(needBorder, true, expandLevel, needRefresh);
        structureConfig.setFile(file);
        return structureConfig;
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

    public VirtualFile getFile() {
        return file;
    }

    public void setFile(VirtualFile file) {
        this.file = file;
    }
}
