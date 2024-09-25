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
    public static final Icon DIFF = load("/icons/diff.svg");
    public static final Icon STAR_FALL_MINI = load("/icons/star_fall_mini.svg");
    public static final Icon XML = load("/icons/xml.svg");
    public static final Icon EXTRACT = load("/icons/extract.svg");

    public static class FileTypes {
        public static final Icon TOML = load("/icons/fileTypes/toml-file.svg");
    }

    public static class ExpUiFileTypes {
        public static final Icon TOML = load("/icons/expui/fileTypes/toml.svg");
    }

    public static class ExpUi {
        public static final Icon NEW_JSON = load("/icons/expui/json.svg");
        public static final Icon NEW_GROUP_BY_CLASS = load("/icons/expui/groupByClass.svg");
        public static final Icon NEW_TOOL_WINDOW_JSON_PATH = load("/icons/expui/toolWindowJsonPath.svg");
        public static final Icon NEW_ROTATE = load("/icons/expui/rotate.svg");
        public static final Icon NEW_EXTRACT = load("/icons/expui/extract.svg");
    }

    /**
     * Json Structure Window
     */
    public static class Structure {

        public static final Icon JSON_KEY = load("/icons/structure/json_key.svg");
        public static final Icon JSON_ARRAY = load("/icons/structure/json_array.svg");
        public static final Icon JSON_ITEM = load("/icons/structure/json_item.svg");
        public static final Icon JSON_OBJECT = load("/icons/structure/json_object.svg");
        public static final Icon JSON_OBJECT_ITEM = load("/icons/structure/json_object_item.svg");
        public static final Icon INTELLIJ_COLLAPSE_ALL = load("/icons/structure/intellij_collapseAll.svg");
        public static final Icon INTELLIJ_EXPAND_ALL = load("/icons/structure/intellij_expandAll.svg");

    }


    /**
     * Toolwindow Action
     */
    public static class ToolWindow {

        public static final Icon DELETE = load("/icons/toolwindow/delete.svg");
        public static final Icon FORMAT = load("/icons/toolwindow/format.svg");
        public static final Icon HISTORY = load("/icons/toolwindow/history.svg");
        public static final Icon MINIFY = load("/icons/toolwindow/minify.svg");
        public static final Icon SEARCH = load("/icons/toolwindow/search.svg");
        public static final Icon STRUCTURE = load("/icons/toolwindow/structure.svg");
        public static final Icon SAVE = load("/icons/toolwindow/save.svg");
        public static final Icon IMPORT_HISTORY = load("/icons/toolwindow/import_history.svg");
        public static final Icon PENCIL = load("/icons/toolwindow/pencil.svg");
        public static final Icon PENCIL_STAR = load("/icons/toolwindow/pencil_star.svg");
        public static final Icon QUERY = load("/icons/toolwindow/query.svg");
        public static final Icon NUMBER = load("/icons/toolwindow/number.svg");

    }

    public static Icon load(String iconPath) {
        return IconLoader.getIcon(iconPath, JsonAssistantIcons.class);
    }

}
