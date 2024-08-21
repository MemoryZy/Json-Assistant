package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class JsonAssistantIcons {

    public static final Icon LOGO = load("/icons/logo.svg");
    public static final Icon BOX = load("/icons/box.svg");
    public static final Icon JSON = load("/icons/json.svg");
    public static final Icon GROUP_BY_CLASS = load("/icons/groupByClass.svg");
    public static final Icon HEART = load("/icons/heart.svg");
    public static final Icon DONATE = load("/icons/donate.svg");
    public static final Icon FLOATING = load("/icons/floating.svg");
    public static final Icon TOOL_WINDOW_JSON_PATH = load("/icons/toolWindowJsonPath.svg");
    public static final Icon WECHAT_PAY = load("/images/wechat_pay.png");
    public static final Icon ALIPAY = load("/images/alipay.png");
    public static final Icon LABEL = load("/icons/label.svg");
    public static final Icon SHORTCUTS = load("/icons/shortcuts.svg");
    public static final Icon FUNCTION = load("/icons/function.svg");
    public static final Icon STRUCTURE = load("/icons/structure.svg");
    public static final Icon CONVERSION = load("/icons/conversion.svg");
    public static final Icon SUN = load("/icons/sun.svg");
    public static final Icon DIZZY_STAR = load("/icons/dizzy_star.svg");
    public static final Icon ROTATE = load("/icons/rotate.svg");
    public static final Icon BOOK = load("/icons/book.svg");
    public static final Icon SEARCH = load("/icons/search.svg");
    public static final Icon HISTORY = load("/icons/history.svg");
    public static final Icon DIFF = load("/icons/diff.svg");
    public static final Icon DELETE = load("/icons/delete.svg");
    public static final Icon SAVE = load("/icons/save.svg");

    public static class ExpUi {
        public static final Icon NEW_JSON = load("/icons/expui/common/json.svg");
        public static final Icon NEW_GROUP_BY_CLASS = load("/icons/expui/common/groupByClass.svg");
        public static final Icon NEW_TOOL_WINDOW_JSON_PATH = load("/icons/expui/common/toolWindowJsonPath.svg");
        public static final Icon NEW_ROTATE = load("/icons/expui/common/rotate.svg");
    }

    /**
     * Json Structure Window
     */
    public static class Structure {

        public static final Icon STRUCTURE = load("/icons/inner_action/structure.svg");
        public static final Icon JSON_KEY = load("/icons/structure/json_key.svg");
        public static final Icon JSON_ARRAY = load("/icons/structure/json_array.svg");
        public static final Icon JSON_ITEM = load("/icons/structure/json_item.svg");
        public static final Icon JSON_OBJECT = load("/icons/structure/json_object.svg");
        public static final Icon JSON_OBJECT_ITEM = load("/icons/structure/json_object_item.svg");

    }

    /**
     * Inner Action
     */
    public static class InnerAction {

        public static final Icon EXPAND_ALL = load("/icons/inner_action/expandAll.svg");
        public static final Icon COLLAPSE_ALL = load("/icons/inner_action/collapseAll.svg");

    }

    public static Icon load(String iconPath) {
        return IconLoader.getIcon(iconPath, JsonAssistantIcons.class);
    }

}
