package cn.memoryzy.json.service.persistent.state.v2;

import com.intellij.util.xmlb.annotations.CollectionBean;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.XCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Memory
 * @since 2025/6/17
 */
public class ToolWindowState {

    // TODO State必须实现equals方法

    public EditorAppearanceStateV2 appearanceState = new EditorAppearanceStateV2();


    public EditorBehaviorStateV2 behaviorState = new EditorBehaviorStateV2();

    @XCollection
    public List<AnnouncementStatsV2> stats = new ArrayList<>();

    @MapAnnotation(surroundWithTag = false)
    public Map<String, AnnouncementStatsV2> statsMap = new HashMap<>();

    private List<String> history = new ArrayList<>();

    @CollectionBean
    public List<String> getHistory() {
        return history;
    }

    public void setHistory(List<String> history) {
        this.history = history;
    }
}
