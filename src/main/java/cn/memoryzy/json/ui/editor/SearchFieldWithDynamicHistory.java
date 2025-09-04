package cn.memoryzy.json.ui.editor;

import com.intellij.ui.SearchTextField;

import java.awt.event.KeyEvent;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 支持动态历史记录的搜索框
 *
 * @author Memory
 * @since 2025/9/4
 */
public class SearchFieldWithDynamicHistory extends SearchTextField {

    /**
     * 确认后要执行的操作
     */
    private final Predicate<String> predicate;

    /**
     * 历史记录持久化 Key 的提供者
     */
    private final Supplier<String> propertyNameSupplier;

    public SearchFieldWithDynamicHistory(Predicate<String> predicate, Supplier<String> propertyNameSupplier) {
        this.predicate = predicate;
        this.propertyNameSupplier = propertyNameSupplier;
        setHistorySize(10);
    }

    @Override
    public void addCurrentTextToHistory() {


        super.addCurrentTextToHistory();
    }


    @Override
    protected boolean preprocessEventForTextField(KeyEvent e) {


        return super.preprocessEventForTextField(e);
    }
}
