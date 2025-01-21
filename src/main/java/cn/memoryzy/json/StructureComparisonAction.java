package cn.memoryzy.json;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.JsonTextDiffAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.editor.SimpleDiffVirtualFile;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2025/1/21
 */
public class StructureComparisonAction extends DumbAwareAction {

    public StructureComparisonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.show.structure.comparison.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.show.structure.comparison.description"));
        presentation.setIcon(AllIcons.Actions.Compile);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        ImmutablePair<String, String> immutablePair = getTwoContent(getDiffContent(dataContext));
        String leftText = immutablePair.getLeft();
        String rightText = immutablePair.getRight();

        // TODO do somethings......

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(isValid(e.getDataContext()));
    }

    private boolean isValid(DataContext dataContext) {
        List<DocumentContent> contentList = getDiffContent(dataContext);
        if (CollUtil.isEmpty(contentList)) {
            return false;
        }

        ImmutablePair<String, String> immutablePair = getTwoContent(contentList);
        String leftText = immutablePair.getLeft();
        String rightText = immutablePair.getRight();

        // 如果文本都为空，那么 enabled 为 false
        boolean blankLeftText = StrUtil.isBlank(leftText);
        boolean blankRightText = StrUtil.isBlank(rightText);
        if (blankLeftText && blankRightText) {
            return false;
        }

        // 检查是否至少有一个文本不为空
        if (!blankLeftText && !JsonUtil.isJson(leftText) && !Json5Util.isJson5(leftText)) {
            return false;
        }

        // 检查是否至少有一个文本不为空
        return blankRightText || JsonUtil.isJson(rightText) || Json5Util.isJson5(rightText);
    }

    private List<DocumentContent> getDiffContent(DataContext dataContext) {
        List<DocumentContent> contentList = new ArrayList<>();
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
        if (virtualFile instanceof SimpleDiffVirtualFile) {
            SimpleDiffRequest diffRequest = virtualFile.getUserData(JsonTextDiffAction.DIFF_REQUEST_KEY);
            if (Objects.isNull(diffRequest)) {
                return contentList;
            }

            List<DiffContent> contents = diffRequest.getContents();
            if (contents.size() != 2) {
                return contentList;
            }

            for (DiffContent content : contents) {
                contentList.add((DocumentContent) content);
            }
        }

        return contentList;
    }

    private ImmutablePair<String, String> getTwoContent(List<DocumentContent> contentList){
        DocumentContent diffContentLeft = contentList.get(0);
        DocumentContent diffContentRight = contentList.get(1);

        String leftText = diffContentLeft.getDocument().getText();
        String rightText = diffContentRight.getDocument().getText();

        return ImmutablePair.of(leftText, rightText);
    }

}
