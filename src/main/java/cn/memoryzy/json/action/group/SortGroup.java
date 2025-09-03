package cn.memoryzy.json.action.group;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class SortGroup extends DefaultActionGroup implements DumbAware, UpdateInBackground {

    private final boolean fromPopup;

    public SortGroup() {
        this(false);
    }

    public SortGroup(boolean fromPopup) {
        super();
        setPopup(true);
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("group.sort.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("group.sort.description"));
        this.fromPopup = fromPopup;
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        boolean showSeparatorText = fromPopup || PlatformUtil.isNewUi();

        List<AnAction> actions = new ArrayList<>();
        // Basic Sorts
        if (showSeparatorText) {
            actions.add(Separator.create(JsonAssistantBundle.message("separator.basic.sorts")));
        }

        actions.add(ActionHolder.Sort.CASE_SENSITIVE_AZ_ACTION);
        actions.add(ActionHolder.Sort.CASE_SENSITIVE_ZA_ACTION);
        actions.add(ActionHolder.Sort.CASE_INSENSITIVE_AZ_ACTION);
        actions.add(ActionHolder.Sort.CASE_INSENSITIVE_ZA_ACTION);
        actions.add(Separator.create());

        // Natural Sorts
        if (showSeparatorText) {
            actions.add(Separator.create(JsonAssistantBundle.message("separator.natural.sorts")));
        }

        actions.add(ActionHolder.Sort.NATURAL_AZ_ACTION);
        actions.add(ActionHolder.Sort.NATURAL_ZA_ACTION);
        actions.add(Separator.create());

        // Special Sorts
        if (showSeparatorText) {
            actions.add(Separator.create(JsonAssistantBundle.message("separator.special.sorts")));
        }

        actions.add(ActionHolder.Sort.LENGTH_ASC_ACTION);
        actions.add(ActionHolder.Sort.LENGTH_DESC_ACTION);
        actions.add(Separator.create());

        actions.add(ActionHolder.Sort.HEXA_ACTION);
        actions.add(ActionHolder.Sort.REVERSE_ACTION);
        actions.add(ActionHolder.Sort.SHUFFLE_ACTION);

        return actions.toArray(new AnAction[0]);
    }



    /*
    Case Sensitive (A-Z)  ---  大小写敏感正序 (A-Z)
    Case Sensitive (Z-A)  ---  大小写敏感倒序 (Z-A)
    Case Insensitive (A-Z)  ---  忽略大小写正序 (A-Z)
    Case Insensitive (Z-A)  ---  忽略大小写倒序 (Z-A)

    Length Ascending      ---    长度升序
    Length Descending      ---    长度降序
    Natural Order (A-Z)        ---     自然排序正序 (A-Z)
    Natural Order (Z-A)        ---     自然排序倒序 (Z-A)
    Hexadecimal         ---   十六进制值排序
    Shuffle   ---   随机
    Reverse   --- 反转


     */
}
