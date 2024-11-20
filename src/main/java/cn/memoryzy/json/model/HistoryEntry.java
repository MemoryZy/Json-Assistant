package cn.memoryzy.json.model;

import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.openapi.editor.actions.ContentChooser;
import com.intellij.openapi.util.text.StringUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/8/11
 */
public class HistoryEntry {

    private final int index;
    private final String shortText;
    private final String longText;
    private final LocalDateTime insertTime;

    public HistoryEntry(int index, String shortText, String longText, LocalDateTime insertTime) {
        this.index = index;
        this.shortText = shortText;
        this.longText = longText;
        this.insertTime = insertTime;
    }

    public int getIndex() {
        return index;
    }

    public String getShortText() {
        return shortText;
    }

    public String getLongText() {
        return longText;
    }

    public LocalDateTime getInsertTime() {
        return insertTime;
    }

    @Override
    public String toString() {
        return shortText;
    }

    public static List<HistoryEntry> of(List<String> historyList) {
        List<HistoryEntry> models = new ArrayList<>();
        for (int i = 0; i < historyList.size(); i++) {
            String jsonStr = historyList.get(i);
            // 解析添加时间
            ImmutablePair<String, LocalDateTime> pair = LimitedList.readAndRemoveInsertTime(jsonStr);

            jsonStr = pair.left;
            LocalDateTime insertTime = pair.right;

            String truncatedText = JsonAssistantUtil.truncateText(jsonStr, 80, "...");
            truncatedText = StringUtil.convertLineSeparators(truncatedText, ContentChooser.RETURN_SYMBOL);
            models.add(new HistoryEntry(i, truncatedText, jsonStr, insertTime));
        }

        return models;
    }

}
