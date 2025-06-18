package cn.memoryzy.json.service.persistent.v2;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.service.persistent.state.v2.JsonRecord;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.XCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Memory
 * @since 2025/6/18
 */
@State(name = "Json Blacklist", storages = {@Storage(value = JsonAssistantPlugin.STORAGE_BLACKLIST_FILE)})
public class BlacklistManager implements PersistentStateComponent<BlacklistManager> {

    /**
     * 最大保留记录数
     */
    private static final int MAX_BLACKLIST_SIZE = 50;

    // public static BlacklistSettings getInstance() {
    //     return ApplicationManager.getApplication().getService(BlacklistSettings.class);
    // }

    public static BlacklistManager getInstance(Project project) {
        return project.getService(BlacklistManager.class);
    }

    /**
     * 黑名单列表
     */
    private List<JsonRecord> blacklist = new ArrayList<>(MAX_BLACKLIST_SIZE);

    @Override
    public @Nullable BlacklistManager getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull BlacklistManager state) {
        this.blacklist = state.blacklist;

        // TODO 可在 loadState方法中加载旧配置
        // migrateFromLegacy()

        /*

            StartupManager.getInstance(myProject).runAfterOpened(() -> {
      ApplicationManager.getApplication().invokeLater(() -> {
        List<Bookmark> newList = myPendingState.getAndSet(null);
        if (newList != null) {
          applyNewState(newList, true);
        }
      }, ModalityState.NON_MODAL, myProject.getDisposed());
    });

         */

        // 加载状态后自动裁剪
        trimBlacklist();
    }

    @XCollection(style = XCollection.Style.v2)
    public List<JsonRecord> getBlacklist() {
        // 返回不可修改的列表保证数据安全
        return Collections.unmodifiableList(blacklist);
    }

    public void setBlacklist(List<JsonRecord> blacklist) {
        this.blacklist = blacklist;
        // 设置新列表后自动裁剪
        trimBlacklist();
    }

    /**
     * 添加新记录到黑名单并进行裁剪
     * @param record 要添加的JSON记录
     */
    public synchronized void add(JsonRecord record) {
        blacklist.add(record);
        trimBlacklist();
    }

    /**
     * 裁剪黑名单到最大保留数量
     */
    private synchronized void trimBlacklist() {
        // 移除最早添加的记录直到满足大小限制
        while (blacklist.size() > MAX_BLACKLIST_SIZE) {
            blacklist.remove(0);
        }
    }
}
