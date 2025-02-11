package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class JsonAssistantIcons {

    public static final Icon BOX = load("/icons/box.svg");
    public static final Icon JSON = load("/icons/json.svg");
    public static final Icon GROUP_BY_CLASS = load("/icons/groupByClass.svg");
    public static final Icon HEART = load("/icons/heart.svg");
    public static final Icon DONATE = load("/icons/donate.svg");
    public static final Icon WECHAT_PAY = load("/images/wechat_pay.png");
    public static final Icon ALIPAY = load("/images/alipay.png");
    public static final Icon LABEL = load("/icons/label.svg");
    public static final Icon SHORTCUTS = load("/icons/shortcuts.svg");
    public static final Icon FUNCTION = load("/icons/function.svg");
    public static final Icon STRUCTURE = load("/icons/structure.svg");
    public static final Icon CONVERSION = load("/icons/conversion.svg");
    public static final Icon SUN = load("/icons/sun.svg");
    public static final Icon DIZZY_STAR = load("/icons/dizzy_star.svg");
    public static final Icon BOOK = load("/icons/book.svg");
    public static final Icon DIFF = load("/icons/diff.svg");
    public static final Icon STAR_FALL_MINI = load("/icons/star_fall_mini.svg");
    public static final Icon ESCAPE = load("/icons/escape.svg");
    public static final Icon ESC = load("/icons/esc.svg");
    public static final Icon BOOK_READER = load("/icons/book_reader.svg");
    public static final Icon GROUP = load("/icons/group.svg");
    public static final Icon CHECKMARK = load("/icons/checkmark.svg");

    public static class FileTypes {
        public static final Icon TOML = load("/icons/fileTypes/toml-file.svg");
        public static final Icon URL = load("/icons/fileTypes/url.svg");
        public static final Icon KV = load("/icons/fileTypes/kv.svg");
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
        public static final Icon COMPARE_STRUCTURE = load("/icons/structure/compare_structure.svg");

    }


    /**
     * Toolwindow Action
     */
    public static class ToolWindow {

        public static final Icon LOGO = load("/icons/toolwindow/logo.svg");
        public static final Icon STRUCTURE_LOGO = load("/icons/toolwindow/structure_logo.svg");
        public static final Icon DELETE = load("/icons/toolwindow/delete.svg");
        public static final Icon HISTORY = load("/icons/toolwindow/history.svg");
        public static final Icon MINIFY = load("/icons/toolwindow/minify.svg");
        public static final Icon SEARCH = load("/icons/toolwindow/search.svg");
        public static final Icon STRUCTURE = load("/icons/toolwindow/structure.svg");
        public static final Icon SAVE = load("/icons/toolwindow/save.svg");
        public static final Icon IMPORT_HISTORY = load("/icons/toolwindow/import_history.svg");
        public static final Icon PENCIL = load("/icons/toolwindow/pencil.svg");
        public static final Icon PENCIL_STAR = load("/icons/toolwindow/pencil_star.svg");
        public static final Icon SETTINGS = load("/icons/toolwindow/settings.svg");
        public static final Icon MAGIC = load("/icons/toolwindow/magic.svg");
        public static final Icon EYE_OFF = load("/icons/toolwindow/eye_off.svg");
        public static final Icon SOFT_WRAP = load("/icons/toolwindow/softWrap.svg");
        public static final Icon SCROLL_DOWN = load("/icons/toolwindow/scrollDown.svg");
        public static final Icon SWITCH = load("/icons/toolwindow/switch.svg");
        public static final Icon TEXT = load("/icons/toolwindow/text.svg");

    }

    public static Icon load(String iconPath) {
        return IconLoader.getIcon(iconPath, JsonAssistantIcons.class);
    }

}
