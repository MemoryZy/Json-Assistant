package cn.memoryzy.json;

/**
 * @author Memory
 * @since 2024/11/1
 */
public class Json5Test {

    /*
    <!--        <dependency>-->
<!--            <groupId>de.marhali</groupId>-->
<!--            <artifactId>json5-java</artifactId>-->
<!--            <version>2.0.0</version>-->
<!--        </dependency>-->
     */

//     /**
//      * 解析JSON5
//      */
//     @Test
//     public void t1() throws IOException {
//         // String json5Data = "{\n" +
//         //         "  \"a\": 'xx', /* xxxx */\n" +
//         //         "  'b': 90, // 开始了\n" +
//         //         "}";
//         String json5Data = "{\n" +
//                 "  // 这是一个注释\n" +
//                 "  unquoted: 'and you can quote me on that',\n" +
//                 "  singleQuotes: 'I can use single quotes',\n" +
//                 "  lineBreaks: \"Look, Mom! \\\n" +
//                 "No \\\\n's!\",\n" +
//                 "  hexadecimal: 0xdecaf,\n" +
//                 "  leadingDecimalPoint: .8675309,\n" +
//                 "  andTrailing: 8675309.,\n" +
//                 "  positiveInfinity: Infinity,\n" +
//                 "  negativeInfinity: -Infinity,\n" +
//                 "  notANumber: NaN,\n" +
//                 "  largeNumber: 1e+100,\n" +
//                 "\n" +
//                 "  arrayWithTrailingComma: [\n" +
//                 "    1,\n" +
//                 "    2,\n" +
//                 "    3,\n" +
//                 "  ],\n" +
//                 "\n" +
//                 "  objectWithTrailingComma: {\n" +
//                 "    one: 1,\n" +
//                 "    two: 2,\n" +
//                 "  },\n" +
//                 "}";
//
//         // Using builder pattern
//         Json5 json5 = Json5.builder(options ->
//                 options.allowInvalidSurrogate().quoteSingle().trailingComma().indentFactor(4).build());
//
//         // Using configuration object
//         // Json5Options options = new Json5Options(true, true, true, 2);
//         // Json5 json5 = new Json5(options);
//
//         // Parse from a String literal
//         Json5Element element =
//                 json5.parse(json5Data/*"{ 'key': 'value', 'array': ['first val','second val'] }"*/);
//
//         Json5Object asJson5Object = element.getAsJson5Object();
//
//         String serialize = json5.serialize(element);
//
//         System.out.println();
//
//
//         // 若要实现
//
//     }

}
