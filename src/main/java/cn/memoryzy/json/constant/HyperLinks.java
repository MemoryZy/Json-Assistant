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
public class HyperLinks {

    public static volatile AtomicBoolean reachableAtomic = new AtomicBoolean(false);

    public static final String GITHUB_LINK = "https://github.com/MemoryZy/Json-Assistant";
    public static final String SUPPORT_LINK = "https://json.memoryzy.cn/support";
    public static final String SPONSOR_LINK = "https://json.memoryzy.cn/support#sponsor";
    public static final String TREE_LINK = "https://json.memoryzy.cn/tree";
    public static final String JSON_TO_JAVA_BEAN_LINK = "https://json.memoryzy.cn/json-to-javabean";
    public static final String OVERVIEW = "https://json.memoryzy.cn/overview";
    public static final String MARKETPLACE_LINK = "https://plugins.jetbrains.com/plugin/24738-json-assistant";
    public static final String MARKETPLACE_REVIEWS_LINK = "https://plugins.jetbrains.com/plugin/24738-json-assistant/reviews";
    public static final String EMAIL_LINK = "memoryzk@outlook.com";
    public static final String PLUGIN_SHARE_LINK = "#PLGUIN_SHARE";
    public static final String PLUGIN_SPONSOR_LINK = "#PLGUIN_SPONSOR";
    public static final String PLUGIN_EMAIL_LINK = "#PLGUIN_MAIL";


    public static void verifyReachable() {
        new Thread(() -> {
            reachableAtomic.getAndSet(isReachable(OVERVIEW));
        }).start();
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
