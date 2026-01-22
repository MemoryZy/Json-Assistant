package cn.memoryzy.json.listener;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.HistoryManager;
import cn.memoryzy.json.service.persistent.state.JsonRecord;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Memory
 * @since 2026/1/21
 */
public class FileChangeListener implements BulkFileListener {

    @Override
    public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
        BulkFileListener.super.before(events);
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        // 在此重新构建 Wrapper 对象，如果解析文本失败，则将wrapper置空，
        for (VFileEvent event : events) {
            // 过滤掉非文件内容变更的事件
            if (event instanceof VFileContentChangeEvent) {
                VirtualFile file = ((VFileContentChangeEvent) event).getFile();
                // 判断是否存在标记
                String projectNameFlag = file.getUserData(PlatformUtil.RECORD_PROJECT_FILE_MARKER);
                if (StrUtil.isNotBlank(projectNameFlag)) {
                    // 找到项目
                    Project project = PlatformUtil.findProject(projectNameFlag);
                    if (null == project) continue;

                    // 找到记录
                    HistoryManager historyManager = HistoryManager.getInstance(project);
                    JsonRecord record = historyManager.findRecord(file.getNameWithoutExtension());
                    if (null == record) continue;

                    // 解析文本
                    String content = FileUtil.readUtf8String(file.getPath());
                    if (StrUtil.isBlank(content)) {
                        record.setWrapper(null);

                    } else {
                        // 解析格式
                        JsonWrapper wrapper = null;
                        if (JsonUtil.isJson(content)) {
                            wrapper = JsonUtil.parse(content);

                        } else if (Json5Util.isJson5(content)) {
                            wrapper = Json5Util.parse(content);
                        }

                        if (null == wrapper || wrapper.noItems()) {
                            record.setWrapper(null);
                        }

                        record.setWrapper(wrapper);
                    }
                }
            }
        }
    }
}
