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
