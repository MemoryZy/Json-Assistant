package cn.memoryzy.json.bundles;

import com.intellij.AbstractBundle;
import com.intellij.DynamicBundle;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Memory
 * @since 2024/6/13
 */
public class JsonAssistantBundle extends AbstractBundle {

    private static final Logger LOG = Logger.getInstance(JsonAssistantBundle.class);

    private static final String BUNDLE = "messages.JsonAssistantBundle";

    private final String pathToBundle;
    private final AbstractBundle adaptedBundle;
    private final ResourceBundle.Control adaptedControl = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

    private static final JsonAssistantBundle INSTANCE = new JsonAssistantBundle(BUNDLE);

    private JsonAssistantBundle(@NonNls @NotNull String pathToBundle) {
        super(pathToBundle);
        this.pathToBundle = pathToBundle;
        this.adaptedBundle = createAdaptedBundle();
    }

    @NotNull
    public static @Nls String message(@NotNull String key, Object... params) {
        return INSTANCE.getAdaptedMessage(key, params);
    }

    private String getAdaptedMessage(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        if (adaptedBundle != null) {
            return adaptedBundle.getMessage(key, params);
        }
        return getMessage(key, params);
    }

    private AbstractBundle createAdaptedBundle() {
        Locale dynamicLocale = getDynamicLocale();
        if (dynamicLocale == null) {
            return null;
        }

        if (dynamicLocale.toLanguageTag().equals(Locale.ENGLISH.toLanguageTag())) {
            return new AbstractBundle(pathToBundle) {
                @Override
                protected @NotNull ResourceBundle findBundle(@NotNull String pathToBundle, @NotNull ClassLoader loader, ResourceBundle.@NotNull Control control) {
                    ResourceBundle dynamicBundle = ResourceBundle.getBundle(pathToBundle, dynamicLocale, loader, adaptedControl);
                    return (dynamicBundle != null) ? dynamicBundle : super.findBundle(pathToBundle, loader, control);
                }
            };
        }

        return null;
    }

    private static Locale getDynamicLocale() {
        try {
            return DynamicBundle.getLocale();
        } catch (Exception e) {
            LOG.debug(e.getClass().getSimpleName() + ": DynamicBundle.getLocale()");
            return null;
        }
    }

}
