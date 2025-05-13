/**
 * Original code modified from tnjson (Unlicense License).
 * Source: https://github.com/anymaker/tnjson
 * Original package: a2u.tn.utils.json
 */
package thirdparty.a2u.tn.utils.json;

import java.util.Map;

/**
 * Using face
 */
public class TnJson {

  private static final TnJsonBuilder SET_HARD         = TnJsonBuilder.init();
  private static final TnJsonBuilder SET_LIGHT        = TnJsonBuilder.init().readable();
  private static final TnJsonBuilder SET_FORMATTED    = TnJsonBuilder.init().readable().formated();
  private static final TnJsonBuilder SET_JSON5        = TnJsonBuilder.init().readable().formated().withoutKeyQuote().allowMultiRowString();
  private static final TnJsonBuilder SET_JSON5COMPACT = TnJsonBuilder.init().readable().withoutKeyQuote().singleQuote();

  /**
   * This name will get element in a returned map when json-string will be an array of values.
   */
  public static final String DEFAULT_LIST_KEY = JsonParser.DEFAULT_LIST_KEY;


  /**
   * JSON generation mode
   * @see #toJson(Object, Mode)
   */
  public enum Mode {
    /**
     * Will be generated compact json-string, where any non-digital and non-letter character in string will be replaced with sequence uXXXX.<br>
     * This mode is default, because it has max compatibility with other clients.
     */
    HARD,

    /**
     * Will be generated compact json-string, where non-digital and non-letter character in string will be stay in readable format, if it possible.<bR>
     * This format is more compact, but is not all client can parse it.
     */
    LIGHT,

    /**
     * Will be generated json-string in pretty read format, where non-digital and non-letter character in string will be stay in readable format, if it possible.<bR>
     *
     */
    FORMATTED,

    /**
     * Will be generated json-string in max human readable format json5.<br>
     * See detail about json5 on https://json5.org/
     */
    JSON5,

    /**
     * JSON5 like, but without linefeed
     */
    JSON5COMPACT
  }

  public static TnJsonBuilder builder() {
    return new TnJsonBuilder();
  }


  /**
   * Convert Object to JSON.<br>
   * Will be generated compact json-string, where any non-digital and non-letter character in string will be replaced with sequence uXXXX.
   * @param data Object to convert it can be Map, Collection, array, or any other object
   * @return JSON-string
   */
  public static String toJson(Object data) {
    return SET_HARD.buildJson(data);
  }

  /**
   * Convert Map to JSON with specify output string format
   * @param data Object to convert. It can be Map, Collection, array, or any other object
   * @param mode affects the format of resulting string
   * @return JSON-string
   * @see Mode
   */
  public static String toJson(Object data, Mode mode) {
    switch (mode) {
      case HARD:         return SET_HARD.buildJson(data);
      case LIGHT:        return SET_LIGHT.buildJson(data);
      case FORMATTED:    return SET_FORMATTED.buildJson(data);
      case JSON5:        return SET_JSON5.buildJson(data);
      case JSON5COMPACT: return SET_JSON5COMPACT.buildJson(data);
      default:           return SET_HARD.buildJson(data);
    }
  }


  /**
   * Parsing JSON-string to Map.<br>
   * Every value in the resulting Map will be:
   * <ul>
   *   <li>or a simple value (string or number or boolean),</li>
   *   <li>or a LinkedHashMap with nested json-object,</li>
   *   <li>or an ArrayList of values.</li>
   * </ul>
   *
   * @param data incoming JSON-string.
   * @return Map with data.<br>
   * If JSON contain only array, such as [1,2] then will return Map
   * with single element by key-name DEFAULT_ROOT, which contain list.
   * @see #DEFAULT_LIST_KEY
   *
   */
  public static Map<String, Object> parse(String data) {
    try {
      JsonParser p = new JsonParser();
      return p.doParse(data);
    }
    catch (ParseException px) {
      //Hide unnecessary log trace. If you want full trace - change it.
      throw new ParseException(px.getMessage(), px.getPosition(), px.getPath());
    }
  }

  /**
   * Parsing JSON-string to Map with specifying returned collections.<br>
   * For each element representing a non-simple value, will be called the corresponding method of listener,
   * which allows you to set type of the returned object.
   * @see IGetCollection
   *
   * @param data incoming JSON-string.
   * @param listener callback listener.
   * @return Map with data.<br>
   * If JSON contain only array, such as [1,2] then will return Map
   * with single element by key-name DEFAULT_LIST_KEY, which contain list.
   * @see JsonParser#DEFAULT_LIST_KEY
   */
  public static Map<String, Object> parse(String data, IGetCollection listener) {
    try {
      JsonParser p = new JsonParser(listener);
      return p.doParse(data);
    }
    catch (ParseException px) {
      //Hide unnecessary log trace. If you want full trace - change it.
      throw new ParseException(px.getMessage(), px.getPosition(), px.getPath());
    }
  }

  public static Map<String, Object> parseWithComment(String data) {
    try {
      JsonParser p = new JsonParser(true);
      return p.doParse(data);
    }
    catch (ParseException px) {
      //Hide unnecessary log trace. If you want full trace - change it.
      throw new ParseException(px.getMessage(), px.getPosition(), px.getPath());
    }
  }

  TnJson() {
    //hide constructor
  }


  /**
   * Build JSON string. As default this method equivalent with static method toJson(), but this method using TnJsonBuilder
   * @param data Object to convert. It can be Map, Collection, array, or any other object
   * @return JSON-string
   */
  public String buildJson(Object data) {
    return SET_HARD.buildJson(data);
  }


}
