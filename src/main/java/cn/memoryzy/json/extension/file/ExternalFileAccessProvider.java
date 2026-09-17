package cn.memoryzy.json.extension.file;

import cn.memoryzy.json.action.toolwindow.OpenFromFileAction;
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessExtension;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/7/10
 */
public class ExternalFileAccessProvider implements NonProjectFileWritingAccessExtension {

    @Override
    public boolean isWritable(@NotNull VirtualFile file) {
        // 被 ExternalFileWrapper 包装的文件被认为不属于外部文件，因此可以被写入
        return file.isValid() && Boolean.TRUE.equals(file.getUserData(OpenFromFileAction.EXTERNAL_FILE_MARKER));
    }

}
