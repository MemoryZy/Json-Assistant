package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionHolder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class ConvertGroup extends DumbAwareBaseActionGroup {

    public ConvertGroup() {
        super(JsonAssistantBundle.message("group.convert.other.formats.text"), JsonAssistantBundle.messageOnSystem("group.convert.other.formats.description"), JsonAssistantIcons.FUNCTION);
        setPopup(true);
        setEnabledInModalContext(true);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent event) {
        List<AnAction> actions = new ArrayList<>();
        actions.add(ActionHolder.Convert.TO_CSV_ACTION);
        actions.add(ActionHolder.Convert.TO_XML_ACTION);
        actions.add(ActionHolder.Convert.TO_YAML_ACTION);
        actions.add(ActionHolder.Convert.TO_TOML_ACTION);
        actions.add(ActionHolder.Convert.TO_JSON5_ACTION);
        actions.add(ActionHolder.Convert.TO_JSON_ACTION);
        actions.add(ActionHolder.Convert.TO_TYPESCRIPT_ACTION);
        actions.add(ActionHolder.Convert.TO_PROPERTIES_ACTION);
        actions.add(ActionHolder.Convert.TO_URL_PARAM_ACTION);
        // Bulk KV Lines：每行一个「键:值」，嵌套用点号与 [下标] 表达
        actions.add(ActionHolder.Convert.TO_BULK_KV_LINES_ACTION);

        // Json 转义：与格式转换不是同一类操作，用分割线与之隔开
        actions.add(Separator.create());
        actions.add(ActionHolder.Main.JSON_ESCAPE_ACTION);

        return actions.toArray(new AnAction[0]);
    }

}
