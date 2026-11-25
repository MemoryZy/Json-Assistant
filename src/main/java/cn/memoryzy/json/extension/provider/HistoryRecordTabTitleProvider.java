package cn.memoryzy.json.extension.provider;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.PathManager;
import cn.memoryzy.json.service.persistent.HistoryManager;
import cn.memoryzy.json.service.persistent.state.JsonRecord;
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 历史记录源文件的编辑器标签标题提供者。
 * <p>
 * 历史记录的源文件位于 {@code .histories/<id>.<ext>}，文件名就是记录 ID（UUID）。
 * 把记录拖到主编辑器区域时，IDE 会把它当普通文件打开，标签标题便是一长串 UUID，难以辨识。
 * 此实现把标题替换为记录自身的名称（未命名时使用记录摘要文本），与历史记录树中的展示保持一致。
 *
 * @author Memory
 * @since 2026/11/25
 */
public class HistoryRecordTabTitleProvider implements EditorTabTitleProvider, DumbAware {

    @Override
    public @Nullable String getEditorTabTitle(@NotNull Project project, @NotNull VirtualFile file) {
        // 先按目录名做一次廉价判定，避免普通文件也去访问历史服务
        VirtualFile parent = file.getParent();
        if (null == parent || !PathManager.PROJECT_HISTORIES_DIRECTORY_NAME.equals(parent.getName())) {
            return null;
        }

        try {
            // 源文件名（不含后缀）即记录 ID
            JsonRecord record = HistoryManager.getInstance(project).findRecord(file.getNameWithoutExtension());
            if (null == record) {
                return null;
            }

            // 记录名称优先，未命名的记录使用摘要文本
            String name = record.getName();
            if (StrUtil.isNotBlank(name)) {
                return name;
            }

            String displayText = record.getDisplayText();
            return StrUtil.isBlank(displayText) ? null : displayText;
        } catch (Exception e) {
            // 标题只用于展示，计算异常时回退为默认文件名
            return null;
        }
    }
}
