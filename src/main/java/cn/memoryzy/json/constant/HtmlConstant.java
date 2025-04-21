package cn.memoryzy.json.constant;

import com.intellij.openapi.util.text.HtmlChunk;

/**
 * @author Memory
 * @since 2024/10/25
 */
public interface HtmlConstant {

    static String wrapHtml(String text) {
        return HtmlChunk.raw(text)
                .wrapWith(HtmlChunk.html())
                .toString();
    }

    static String wrapBody(String text) {
        return HtmlChunk.raw(text)
                .wrapWith(HtmlChunk.body())
                .wrapWith(HtmlChunk.html())
                .toString();
    }

    static String wrapBoldHtml(String text) {
        return HtmlChunk.raw(text)
                .bold()
                .wrapWith(HtmlChunk.body())
                .wrapWith(HtmlChunk.html())
                .toString();
    }


}
