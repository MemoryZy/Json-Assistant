package cn.memoryzy.json.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class HttpUtil {

    // 连接超时 10 秒，不设置整体超时（允许长时间下载）
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * GET 请求（响应字符串，UTF-8），整体超时 30 秒（一般响应很快）
     */
    public static String get(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept-Charset", "UTF-8")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                throw new RuntimeException("GET 失败，状态码：" + response.statusCode());
            }
            return response.body();

        } catch (Exception e) {
            throw new RuntimeException("GET 请求异常：" + e.getMessage(), e);
        }
    }

    /**
     * 下载文件到本地（无整体超时限制，只依赖连接超时）
     */
    public static long downloadFile(String fileUrl, File destFile) {
        File parent = destFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try {
            // 注意：这里没有设置 .timeout()，表示不限制总下载时间
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fileUrl))
                    .timeout(Duration.ofMinutes(30))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                if (destFile.exists()) destFile.delete();
                throw new RuntimeException("下载失败，状态码：" + response.statusCode());
            }

            try (InputStream in = response.body();
                 FileOutputStream out = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[8192];
                int len;
                long total = 0;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    total += len;
                }
                out.flush();
                return total;
            }

        } catch (Exception e) {
            if (destFile.exists()) destFile.delete();
            throw new RuntimeException("下载异常：" + e.getMessage(), e);
        }
    }
}