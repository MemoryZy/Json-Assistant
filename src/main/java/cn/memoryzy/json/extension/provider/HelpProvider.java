package cn.memoryzy.json.extension.provider;

import cn.memoryzy.json.enums.UrlType;
import com.intellij.openapi.help.WebHelpProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/10/25
 */
public class HelpProvider extends WebHelpProvider {
    @Override
    public @Nullable String getHelpPageUrl(@NotNull String helpTopicId) {
        return UrlType.of(helpTopicId).getUrl();
    }
}
