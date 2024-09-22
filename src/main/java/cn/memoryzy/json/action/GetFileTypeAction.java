package cn.memoryzy.json.action;

import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/9/21
 */
public class GetFileTypeAction extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        Editor editor = PlatformUtil.getEditor(e);
        Document document = editor.getDocument();

        FileType fileType = PlatformUtil.getDocumentFileType(project, document);

        FileType yaml = FileTypeHolder.YAML;
        FileType toml = FileTypeHolder.TOML;
        FileType xml = FileTypeHolder.XML;

        Messages.showInfoMessage("类型：" + fileType, "");
    }

}
