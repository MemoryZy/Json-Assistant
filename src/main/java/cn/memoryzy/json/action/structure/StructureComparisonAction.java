package cn.memoryzy.json.action.structure;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.JsonTextDiffAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Memory
 * @since 2025/1/21
 */
public class StructureComparisonAction extends DumbAwareAction {

    public StructureComparisonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.structure.comparison.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.structure.comparison.description"));
        presentation.setIcon(JsonAssistantIcons.Structure.COMPARE_STRUCTURE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {


        // DataContext dataContext = event.getDataContext();
        // ImmutablePair<String, String> pair = getTwoContent(getDiffContent(dataContext));

        // JsonWrapper leftObj = parseJson(pair.getLeft());
        // JsonWrapper rightObj = parseJson(pair.getRight());

        // DualTreeComponentProvider provider = new DualTreeComponentProvider(leftObj, rightObj);

        // JsonStructureComponentProvider leftProvider = new JsonStructureComponentProvider(leftObj, null, false, false);

        // DialogBuilder dialogBuilder = new DialogBuilder();
        // dialogBuilder.centerPanel(provider.createComponent());
        // dialogBuilder.show();

        // TODO do somethings......

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(isValid(e.getDataContext()));
    }

    public static JsonWrapper parseJson(String json) {
        if (StrUtil.isNotBlank(json)) {
            if (JsonUtil.isJson(json)) {
                return JsonUtil.parse(json);
            } else {
                return Json5Util.parse(json);
            }
        }

        return null;
    }

    private boolean isValid(DataContext dataContext) {
        List<DocumentContent> contentList = JsonTextDiffAction.getDiffContent(dataContext);
        if (CollUtil.isEmpty(contentList)) {
            return false;
        }

        ImmutablePair<String, String> immutablePair = JsonTextDiffAction.getContent(contentList);
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

}
