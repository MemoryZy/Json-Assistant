package cn.memoryzy.json.constant;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Memory
 * @since 2024/7/25
 */
public class Urls {

    public static final AtomicBoolean reachableAtomic = new AtomicBoolean(false);

    public static final String GITHUB_LINK = "https://github.com/MemoryZy/Json-Assistant";
    public static final String GITHUB_ISSUE_LINK = "https://github.com/MemoryZy/Json-Assistant/issues/new";
    public static final String SUPPORT_LINK = "https://json.memoryzy.cn/support";
    public static final String DONATE_LINK = "https://json.memoryzy.cn/support#donate";
    public static final String DONORS_LIST_LINK = "https://json.memoryzy.cn/support#donors-list";
    public static final String TREE_LINK = "https://json.memoryzy.cn/tree";
    public static final String JSON_TO_JAVA_BEAN_LINK = "https://json.memoryzy.cn/deserialization";
    public static final String RECOGNIZE = "https://json.memoryzy.cn/view#recognize";
    public static final String HISTORY = "https://json.memoryzy.cn/view#history";
    public static final String YAML_DIALOG = "https://json.memoryzy.cn/conversion#yaml";
    public static final String OVERVIEW = "https://json.memoryzy.cn/overview";
    public static final String VIEW = "https://json.memoryzy.cn/view";
    public static final String MARKETPLACE_LINK = "https://plugins.jetbrains.com/plugin/24738-json-assistant";
    public static final String MARKETPLACE_REVIEWS_LINK = "https://plugins.jetbrains.com/plugin/24738-json-assistant/reviews";
    public static final String EMAIL_LINK = "memoryzk@outlook.com";

    public static final String JSONPATH_EXPRESS_DESCRIPTION = "https://goessner.net/articles/JsonPath/";
    public static final String JMESPATH_EXPRESS_DESCRIPTION = "https://jmespath.org/";
    public static final String JSON5_SITE_LINK = "https://json5.org/";

    public static final String FRONT_URL = "http://0.0.0.0";

    public static void verifyReachable() {
        new Thread(() -> reachableAtomic.getAndSet(isReachable(OVERVIEW))).start();
    }

    public static boolean isReachable() {
        return reachableAtomic.get();
    }

    /**
     * 验证地址是否可达
     *
     * @param url 网络地址
     * @return 可达，true；否则为 false
     */
    public static boolean isReachable(String url) {
        return isReachable(url, 5000);
    }

    /**
     * 验证地址是否可达
     *
     * @param url 网络地址
     * @return 可达，true；否则为 false
     */
    public static boolean isReachable(String url, int timeout) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            RequestConfig requestConfig = RequestConfig.custom()
                    // 设置连接超时时间
                    .setConnectTimeout(timeout)
                    // 设置读取超时时间
                    .setSocketTimeout(timeout)
                    .build();
            HttpGet request = new HttpGet(url);
            request.setConfig(requestConfig);
            httpClient.execute(request);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
