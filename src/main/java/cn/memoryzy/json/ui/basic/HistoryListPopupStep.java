package cn.memoryzy.json.ui.basic;

import cn.memoryzy.json.model.HistoryModel;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Memory
 * @since 2024/8/11
 */
public class HistoryListPopupStep extends BaseListPopupStep<HistoryModel> {

    public HistoryListPopupStep(@Nullable String title, List<? extends HistoryModel> values) {
        super(title, values);
    }

    @Override
    public @Nullable PopupStep<?> onChosen(HistoryModel selectedValue, boolean finalChoice) {
        int i = getValues().indexOf(selectedValue);
        return super.onChosen(selectedValue, finalChoice);
    }

}
