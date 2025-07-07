package cn.memoryzy.json.service.persistent.state;

import cn.memoryzy.json.enums.ColorScheme;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.util.xmlb.annotations.Tag;

/**
 * 编辑器外观设置项
 *
 * @author Memory
 * @since 2025/6/17
 */
@Tag("editor-visual")
public class EditorVisualState {

    /**
     * 是否在编辑器中显示行号
     */
    private boolean showLineNumbers = false;

    /**
     * 是否显示代码块的折叠控制符号
     */
    private boolean showFoldingOutline = PlatformUtil.isNewUi();

    /**
     * 编辑器配色方案名称
     */
    private ColorScheme colorScheme = ColorScheme.Default;


    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
    }

    public void setShowFoldingOutline(boolean showFoldingOutline) {
        this.showFoldingOutline = showFoldingOutline;
    }

    public void setColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }


    public boolean isShowLineNumbers() {
        return showLineNumbers;
    }

    public boolean isShowFoldingOutline() {
        return showFoldingOutline;
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

}
