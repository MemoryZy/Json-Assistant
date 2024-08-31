package cn.memoryzy.json.model;

import cn.memoryzy.json.util.JsonAssistantUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/8/11
 */
public class HistoryModel {

    private final int index;
    private final String abbreviatedContent;
    private final String wholeContent;

    public HistoryModel(int index, String abbreviatedContent, String wholeContent) {
        this.index = index;
        this.abbreviatedContent = abbreviatedContent;
        this.wholeContent = wholeContent;
    }

    public int getIndex() {
        return index;
    }

    public String getAbbreviatedContent() {
        return abbreviatedContent;
    }

    public String getWholeContent() {
        return wholeContent;
    }

    @Override
    public String toString() {
        return abbreviatedContent;
    }

    public static List<HistoryModel> of(List<String> historyList){
        List<HistoryModel> models = new ArrayList<>();
        for (int i = 0; i < historyList.size(); i++) {
            String element = historyList.get(i);
            models.add(new HistoryModel(i, JsonAssistantUtil.truncateText(element, 45, "..."), element));
        }

        return models;
    }

}
