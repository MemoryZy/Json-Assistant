package cn.memoryzy.json.util;

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Memory
 * @since 2024/8/5
 */
public class XmlUtil {

    /**
     * 线程安全的工厂实例
     */
    private static final WstxInputFactory factory = new WstxInputFactory();

    static {
        factory.configureForSpeed();
        // 提升性能
        factory.setProperty(XMLInputFactory.IS_COALESCING, false);
        // 禁用DTD
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    /**
     * 通用 XML 格式验证方法
     *
     * @param xmlText 需要验证的 XML 内容
     * @return 当且仅当 XML 格式完全正确时返回true
     */
    public static boolean isXML(String xmlText) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(
                xmlText.getBytes(StandardCharsets.UTF_8))) {

            XMLStreamReader reader = factory.createXMLStreamReader(
                    new BufferedInputStream(is, 256 * 1024));

            int depth = 0;
            boolean hasRoot = false;

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        depth++;
                        hasRoot = true; // 只要存在元素即认为有根
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (--depth < 0) {
                            return false; // 闭合标签多于开始标签
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        // 检查非法控制字符
                        String text = reader.getText();
                        if (text.chars().anyMatch(c -> c <= 0x1F && c != 0x9 && c != 0xA && c != 0xD)) {
                            return false;
                        }
                        break;
                }
            }

            return hasRoot && depth == 0; // 存在根元素且标签完全闭合
        } catch (Exception e) {
            return false;
        }
    }


    public static String toXml(String jsonStr, boolean isJson) throws Exception {
        Object object = isJson
                ? JsonUtil.parse(jsonStr)
                : Json5Util.parse(jsonStr);
        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.writerWithDefaultPrettyPrinter()
                .withRootName("root")
                .writeValueAsString(object);
    }

    public static String toJson(String xmlStr) {
        try {
            ObjectMapper xmlMapper = new XmlMapper();
            JsonNode jsonNode = xmlMapper.readTree(xmlStr.getBytes(StandardCharsets.UTF_8));
            return JsonUtil.toJsonStr(jsonNode);
        } catch (Exception e) {
            return null;
        }
    }

}
