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


    String TIMEOUT_HTML = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <title>__TITLE__</title>\n" +
            "    <meta charset=\"utf-8\" />\n" +
            "    <meta http-equiv=\"x-ua-compatible\" content=\"IE=edge\" />\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, maximum-scale=1\" />\n" +
            "\n" +
            "    <style>\n" +
            "        *, *::after {\n" +
            "            box-sizing: border-box;\n" +
            "        }\n" +
            "\n" +
            "        html, body, p {\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "        }\n" +
            "        html, body {\n" +
            "            height: 100%;\n" +
            "        }\n" +
            "        body {\n" +
            "            background-color: #fff;\n" +
            "        }\n" +
            "\n" +
            "        .container {\n" +
            "            box-sizing: border-box;\n" +
            "            width: 100%;\n" +
            "            max-width: 1276px;\n" +
            "            margin-right: auto;\n" +
            "            margin-left: auto;\n" +
            "            padding-right: 22px;\n" +
            "            padding-left: 22px;\n" +
            "        }\n" +
            "        .content {\n" +
            "            width: calc(100% / 12 * 6);\n" +
            "            margin-left: calc(100% / 12 * 3);\n" +
            "        }\n" +
            "\n" +
            "        .text {\n" +
            "            letter-spacing: normal;\n" +
            "            color: rgba(39, 40, 44, 0.7);\n" +
            "            font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Droid Sans', 'Helvetica Neue', Arial, sans-serif;\n" +
            "            font-size: 15px;\n" +
            "            font-weight: normal;\n" +
            "            font-style: normal;\n" +
            "            font-stretch: normal;\n" +
            "            line-height: 1.6;\n" +
            "        }\n" +
            "\n" +
            "        .title {\n" +
            "            letter-spacing: normal;\n" +
            "            color: #27282c;\n" +
            "            font-family: -apple-system, Helvetica, system-ui, BlinkMacSystemFont, Segoe UI, Roboto, Oxygen, Ubuntu, Cantarell, Droid Sans, Helvetica Neue, Arial, sans-serif;\n" +
            "            font-weight: bold;\n" +
            "            font-style: normal;\n" +
            "            font-stretch: normal;\n" +
            "        }\n" +
            "\n" +
            "        .title_h2 {\n" +
            "            font-size: 31px;\n" +
            "            line-height: 1.3;\n" +
            "        }\n" +
            "\n" +
            "        .offset-12 {\n" +
            "            margin-top: 12px;\n" +
            "        }\n" +
            "        .offset-24 {\n" +
            "            margin-top: 24px;\n" +
            "        }\n" +
            "\n" +
            "        /*noinspection CssUnusedSymbol*/\n" +
            "        .link {\n" +
            "            outline: none;\n" +
            "            cursor: pointer;\n" +
            "            font-size: inherit;\n" +
            "            line-height: inherit;\n" +
            "            border-bottom: 1px solid transparent;\n" +
            "        }\n" +
            "        /*noinspection CssUnusedSymbol*/\n" +
            "        .link, .link:hover {\n" +
            "            text-decoration: none;\n" +
            "        }\n" +
            "        /*noinspection CssUnusedSymbol*/\n" +
            "        .link:hover {\n" +
            "            border-bottom-color: currentColor;\n" +
            "        }\n" +
            "        /*noinspection CssUnusedSymbol*/\n" +
            "        .link, .link:hover, .link:active, .link:focus {\n" +
            "            color: #167dff;\n" +
            "        }\n" +
            "\n" +
            "        .section {\n" +
            "            padding-bottom: 48px;\n" +
            "            padding-top: 1px;\n" +
            "            background: #fff;\n" +
            "        }\n" +
            "\n" +
            "        /*noinspection CssUnusedSymbol*/\n" +
            "        .theme-dark, .theme-dark .section {\n" +
            "            background: #27282c;\n" +
            "        }\n" +
            "        .theme-dark .title {\n" +
            "            color: #fff;\n" +
            "        }\n" +
            "        .theme-dark .text {\n" +
            "            color: rgba(255, 255, 255, 0.60);\n" +
            "        }\n" +
            "        /*noinspection CssUnusedSymbol*/\n" +
            "        .theme-dark .link {\n" +
            "            color: rgb(76, 166, 255);\n" +
            "        }\n" +
            "\n" +
            "        @media screen and (max-width: 1276px) {\n" +
            "            .container {\n" +
            "                max-width: 996px;\n" +
            "                padding-right: 22px;\n" +
            "                padding-left: 22px;\n" +
            "            }\n" +
            "            .content {\n" +
            "                width: calc(100% / 12 * 8);\n" +
            "                margin-left: calc(100% / 12 * 2);\n" +
            "            }\n" +
            "        }\n" +
            "        @media screen and (max-width: 1000px) {\n" +
            "            .container {\n" +
            "                max-width: 100%;\n" +
            "            }\n" +
            "            .content {\n" +
            "                width: calc(100% / 12 * 10);\n" +
            "                margin-left: calc(100% / 12 * 1);\n" +
            "            }\n" +
            "        }\n" +
            "        @media screen and (max-width: 640px) {\n" +
            "            .container {\n" +
            "                padding-right: 16px;\n" +
            "                padding-left: 16px;\n" +
            "            }\n" +
            "            .content {\n" +
            "                width: calc(100% / 12 * 12);\n" +
            "                margin-left: calc(100% / 12 * 0);\n" +
            "            }\n" +
            "            .offset-12 {\n" +
            "                margin-top: 8px;\n" +
            "            }\n" +
            "            .offset-24 {\n" +
            "                margin-top: 16px;\n" +
            "            }\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body class=\"__THEME__\">\n" +
            "<section class=\"section\">\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"content\">\n" +
            "            <h2 class=\"title title_h2 offset-24\">__TITLE__</h2>\n" +
            "            <p class=\"text offset-24\">__MESSAGE__</p>\n" +
            "            <p class=\"text offset-12\">__ACTION__</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</section>\n" +
            "</body>\n" +
            "</html>";

}
