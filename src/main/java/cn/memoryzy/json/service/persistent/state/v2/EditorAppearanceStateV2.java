package cn.memoryzy.json.service.persistent.state.v2;

import cn.memoryzy.json.enums.ColorScheme;
import cn.memoryzy.json.util.PlatformUtil;

/**
 * @author Memory
 * @since 2025/6/17
 */
// @Tag("properties")
public class EditorAppearanceStateV2 {

    /**
     * 是否在编辑器中显示行号
     */
    private boolean displayLineNumbers = false;

    /**
     * 是否显示代码块的折叠控制符号
     */
    private boolean showFoldingOutline = PlatformUtil.isNewUi();

    /**
     * 编辑器配色方案名称
     */
    private String colorScheme = ColorScheme.Default.name();


    public void setDisplayLineNumbers(boolean displayLineNumbers) {
        this.displayLineNumbers = displayLineNumbers;
    }

    public void setShowFoldingOutline(boolean showFoldingOutline) {
        this.showFoldingOutline = showFoldingOutline;
    }

    public void setColorScheme(String colorScheme) {
        this.colorScheme = colorScheme;
    }

    public boolean isDisplayLineNumbers() {
        return displayLineNumbers;
    }

    public boolean isShowFoldingOutline() {
        return showFoldingOutline;
    }

    public String getColorScheme() {
        return colorScheme;
    }
}
