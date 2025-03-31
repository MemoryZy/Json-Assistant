package thirdparty.a2u.tn.utils.json;

import java.math.BigInteger;
import java.util.*;

/**
 * Parsing JSON string to Map object with JSON5
 * JSON for Humans https://spec.json5.org
 *
 */
class JsonParser {

  /**
   * This name will get element in a returned map when json-string will be an array of values.
   */
  public static final String DEFAULT_LIST_KEY = "list";

  /**
   * Name of root element in a path. Using in call-back methods of IGetCollection.
   * @see IGetCollection#forObject(String)
   * @see IGetCollection#forList(String)
   */
  public static final String PATH_ROOT_KEY = "root";

  // 使用包含非标准字符的组合键名（符合JSON5键名规范）
  private static final String COMMENT_MARKER = "#$__comments__";

  private IGetCollection listener;

  private String lastComment = null; // 存储最近捕获的注释

  /**
   * Inner immutable class for represent path by root of json-object
   */
  private class Path {
    private String path;

    public Path(String str) {
      this.path = str;
    }

    public String getName() {
      return path;
    }

    public Path add(String node) {
      return new Path(path + '.' + node);
    }

  }

  private static final String NULL = "null";
  private static final String BOOL_TRUE = "true";
  private static final String BOOL_FALSE = "false";
  private static final String NUM_INFINITY = "infinity";
  private static final String NUM_INFINITY_PSITIVE = "+infinity";
  private static final String NUM_INFINITY_NEGATIVE = "-infinity";
  private static final String NUM_NAN = "nan";

  private static final char LF = 0x0A;
  private static final char CR = 0x0D;
  private static final char LS = 0x2028;
  private static final char PS = 0x2029;


  private Map<String, Object> resultMap; // result object
  private String content;                // incoming json-string
  private int maxLength;                 // cache of length the incoming json-string
  private int index;                     // current accepted symbol


  JsonParser() {
    // hide this
  }
  JsonParser(IGetCollection listener) {
    this.listener = listener;
  }



  /**
   * Prepare and start parsing
   * @param data json-string
   * @return java-map object - result of parsing
   */
  Map<String, Object> doParse(String data) {
    content = data.trim();
    maxLength = content.length();
    index = 0;
    lastComment = null;

    Path emptypath = new Path(PATH_ROOT_KEY);
    resultMap = getCollectionForObject(emptypath);

    parseEmpty(emptypath);

    return resultMap;
  }

  private void parseEmpty(Path emptypath) {
    while (index < maxLength) {
      char c = getTokenBegin();
      if (c == '{') {
        index++;
        resultMap = parseMap(emptypath);
        return;
      }
      else if (c == '[') {
        index++;
        Collection list = parseList(emptypath);
        resultMap.put(DEFAULT_LIST_KEY, list);
      }

      index++;
    }
  }

  private Map<String, Object> parseMap(Path path) {
    Map<String, Object> map = getCollectionForObject(path);

    while (index < maxLength) {
      char c = getTokenBegin(); // skip to begin

      if (c == '}') {
        index++;
        return map;
      }

      // 捕获键前注释
      String keyComment = lastComment;
      lastComment = null;

      String key = extractIdenty(path);
      if (COMMENT_MARKER.equals(key)) {
        throw new ParseException("Key '" + COMMENT_MARKER + "' is reserved for internal use", index, path.getName());
      }

      if (keyComment != null) {
        addCommentToMap(map, key, keyComment, path);
      }

      String pathForLog = path.getName() + "." + key;

      c = getTokenBegin();
      if (c != ':') {
        throw new ParseException("Invalid character '" + charToLog(c) + "' at position " + index + ", path '" + pathForLog + "', expected ':'.", index, pathForLog);
      }
      index++;
      Object val = extractValue(path.add(key));
      map.put(key, val);

      // 处理行内注释（值后）
      if (val != null) {
        checkInlineComment(map, key, path);
      }

      c = getTokenBegin();
      if (c == '}') {
        index++;
        return map;
      }
      if (c == ',') {
        index++;
        continue;
      }

      throw new ParseException("Invalid character '" + charToLog(c) + "' at position " + index + ", last path '"+ pathForLog +"', expected ',' or '}'.", index, pathForLog);
    }

    return map;
  }

  @SuppressWarnings("unchecked")
  private Collection parseList(Path path) {
    Collection list = getCollectionForList(path);
    int elementIndex = 0;
    Map<String, Object> parentMap = getParentMapForList(path);

    while (index < maxLength) {
      char c = getTokenBegin();

      if (c == ']') {
        index++;
        return list;
      }

      if (c == ',') {
        index++;
        continue;
      }

      // 捕获元素前注释
      String elementComment = lastComment;
      lastComment = null;

      Object val = extractValue(path);
      list.add(val);

      if (elementComment != null && parentMap != null) {
        addCommentToMap(parentMap, String.valueOf(elementIndex), elementComment, path);
      }

      elementIndex++;
    }

    return list;
  }

  // 获取列表的父Map（假设路径最后为列表键）
  private Map<String, Object> getParentMapForList(Path path) {
    // 实现根据路径查找父Map的逻辑，此处简化为resultMap
    return resultMap;
  }

  private void addCommentToMap(Map<String, Object> map, String key, String comment, Path path) {
    // 检查冲突
    if (map.containsKey(COMMENT_MARKER)) {
      Object existing = map.get(COMMENT_MARKER);
      if (!(existing instanceof Map)) {
        throw new ParseException("Conflict with reserved key: " + COMMENT_MARKER, index, path.getName());
      }
    }

    @SuppressWarnings("unchecked")
    Map<String, String> comments = (Map<String, String>) map.get(COMMENT_MARKER);
    if (comments == null) {
      comments = new HashMap<>();
      map.put(COMMENT_MARKER, comments);
    }
    comments.put(key, comment);
  }

  // 检查值后的行内注释
  private void checkInlineComment(Map<String, Object> map, String key, Path path) {
    int currentIndex = index;
    while (currentIndex < maxLength) {
      char c = content.charAt(currentIndex);
      if (c == '/') {
        if (currentIndex + 1 < maxLength && content.charAt(currentIndex + 1) == '/') {
          int commentStart = currentIndex + 2;
          int commentEnd = findLineEnd(commentStart);
          String inlineComment = content.substring(commentStart, commentEnd).trim();
          addCommentToMap(map, key, inlineComment, path);
          index = commentEnd; // 更新索引
          break;
        }
      } else if (!isWhiteSpace(c)) {
        break;
      }
      currentIndex++;
    }
  }

  private int findLineEnd(int start) {
    while (start < maxLength && !isLineTerminator(content.charAt(start))) {
      start++;
    }
    return start;
  }

  /**
   * Return a first symbol which has mean, starting at position = index.
   * Comments will be ignored.
   * index will point to this symbol.
   */
  private char getTokenBegin() {
    while (index < maxLength) {
      char c = content.charAt(index);

      if (c == '/') {
        if (index + 1 >= maxLength) return 0;
        char next = content.charAt(index + 1);
        if (next == '/') {
          skipToEndLine();
          continue;
        } else if (next == '*') {
          skipToEndComent();
          continue;
        }
      }

      if (Character.isLetterOrDigit(c) || c == '"' || c == '\'' || c == '@' || c == '#' || c == '$' || c == '_' || c == '{' || c == '}' || c == ':' || c == '[' || c == ']' || c == ',' || c == '+' || c == '-' || c == '.') {
        return c;
      }

      if (c == '\\') {
        return c;
      }

      index++;
    }
    return 0;
  }

  private void skipToEndLine() {
    int start = index + 2; // 跳过 '//'
    while (index < maxLength) {
      char c = content.charAt(index);
      if (isLineTerminator(c)) {
        lastComment = content.substring(start, index).trim();
        index++;
        return;
      }
      index++;
    }
    lastComment = content.substring(start).trim();
  }

  private void skipToEndComent() {
    int start = index + 2; // 跳过 '/*'
    while (index < maxLength) {
      if (content.charAt(index) == '*' && index + 1 < maxLength && content.charAt(index + 1) == '/') {
        lastComment = content.substring(start, index).trim();
        index += 2;
        return;
      }
      index++;
    }
    lastComment = content.substring(start).trim();
  }



  private String extractIdenty(Path path) {
    char c = content.charAt(index);
    char terminator = (c == '"' || c == '\'') ? c : 0;

    StringBuilder b = new StringBuilder();
    while (index < maxLength) {
      c = content.charAt(index);
      if (c == terminator) {
        index++;
        if (b.length() == 0) {
          continue;
        }
        return b.toString().trim();
      }
      if (terminator == 0 && (c == ':' || c == '/' || isWhiteSpace(c))) {
        return b.toString().trim();
      }

      if (c  == '\\') {
        c = getCharFromEscapedText();
      }
      if ((terminator == 0 && (c== '\'' || c == '"')) || isLineTerminator(c)) {
        throw new ParseException("Invalid character '"+ charToLog(c) +"' for identifier '"+ b.toString() +"' at position "+ index +", path '"+ path.getName() +"'.", index, path.getName());
      }
      else {
        b.append(c);
      }
      index++;
    }

    return b.toString().trim();
  }

  private Object extractValue(Path path) {
    char c = getTokenBegin();

    if (c == '{') {
      index++;
      Map<String, ?> map = parseMap(path);
      return map;
    }
    if (c == '[') {
      index++;
      Collection list = parseList(path);
      return list;
    }
    if (c == '"' || c == '\'') {
      String str = extractString();
      return str;
    }
    else {
      Object num = extractLiteral(path);
      return num;
    }

  }


  private Object extractLiteral(Path path) {
    StringBuilder b = new StringBuilder();
    while (index < maxLength) {
      char c = content.charAt(index);
      if (c == '+' && b.length() == 0) {
        index++;
      }
      else if (Character.isLetterOrDigit(c) || c == '.' || c == '+' || c == '-') {
        b.append(c);
        index++;
      }
      else {
        break;
      }
    }

    String literal = b.toString().trim().toLowerCase();

    switch (literal) {
      case NULL:
        return null;
      case BOOL_TRUE:
        return true;
      case BOOL_FALSE:
        return false;
      case NUM_INFINITY:
        return Double.POSITIVE_INFINITY;
      case NUM_INFINITY_PSITIVE:
        return Double.POSITIVE_INFINITY;
      case NUM_INFINITY_NEGATIVE:
        return Double.NEGATIVE_INFINITY;
      case NUM_NAN:
        return Double.NaN;
    }

    try {
      return detectNumber(literal);
    }
    catch (Exception ex) {
      throw new ParseException("Invalid literal '" + literal + "' at position " + index + ", path '"+ path.getName() +"'.", index, path.getName());
    }

  }

  private Object detectNumber(String literal) {
    boolean hasDot = literal.indexOf('.') >= 0;
    boolean hasE = literal.indexOf('e') >= 0;
    boolean hasX = literal.indexOf('x') >= 0;

    if (hasDot || (hasE && ! hasX)) {
      return Double.parseDouble(literal);
    }

    // Integer.MAX_VALUE dec == 2147483647 - 10 characters
    // Integer.MAX_VALUE hex == 0x7fffffff - 8 characters + 2 (0x) = 10 characters
    // Long.MAX_VALUE dec == 9223372036854775807 - 19 characters
    // Long.MAX_VALUE hex == 0x7fffffffffffffff - 16 characters + 2 (0x) = 18 characters
    //
    // therefore
    // for maxintlen == 9 or 9 characters dec / hex
    // for maxlonglen == 18 or 17 characters dec / hex
    int maxintlen = hasX ? 9 : 9;
    int maxlonglen = hasX ? 17 : 18;
    if (literal.charAt(0) == '-') {
      maxintlen++;
      maxlonglen++;
    }
    if (literal.length() <= maxintlen) {
      return Integer.decode(literal);
    }
    if (literal.length() <= maxlonglen) {
      return Long.decode(literal);
    }
    else {
      return new BigInteger(literal);
    }
  }


  private String extractString() {
    char terminator = content.charAt(index);
    index++;

    StringBuilder b = new StringBuilder();
    while (index < maxLength) {
      char c = content.charAt(index);
      if (c == '\\') {
        char ce = getCharFromEscapedText();
        b.append(ce);
        index++;
      }
      else if (c == terminator) {
        index++;
        break;
      }
      else {
        b.append(c);
        index++;
      }
    }

    return b.toString().trim();
  }

  /**
   * Extract escaped sequences, and convert to char
   * https://spec.json5.org/#escapes
   *
   * As result index will refer to the last accepted character
   *
   * @return matching character
   */
  private char getCharFromEscapedText() {
    char resultChar;
    index++;
    char next = content.charAt(index);
    switch (next) {
      case 'b': resultChar = '\b'; break;
      case 'f': resultChar = '\f'; break;
      case 'n': resultChar = '\n'; break;
      case 'r': resultChar = '\r'; break;
      case 't': resultChar = '\t'; break;
      case 'v': resultChar = 0x000B; break;
      case '0': resultChar = 0x0000; break;
      case '\'': resultChar = '\'';  break;
      case '"': resultChar = '"';  break;
      case '\\': resultChar = '\\'; break;
      case 'u':
        StringBuilder bu = new StringBuilder();
        bu.append(content.charAt(++index));
        bu.append(content.charAt(++index));
        bu.append(content.charAt(++index));
        bu.append(content.charAt(++index));
        int hexValU = Integer.parseInt(bu.toString(), 16);
        resultChar = (char) hexValU;
        break;

      case 'x':
      case 'X':
        StringBuilder bx = new StringBuilder();
        char cx = content.charAt(index + 1); // index will refer to the last accepted character, therefore we need to use +1
        while (isHexadecimalChar(cx)) {
          bx.append(cx);
          index++;
          cx = content.charAt(index + 1);
        }
        int hexValX = Integer.parseInt(bx.toString(), 16);
        resultChar = (char) hexValX;
        break;

      default: return next;

    }

    return resultChar;
  }


  /**
   * Line terminator point out to end of single-line comment
   */
  private boolean isLineTerminator(char c) {
    return c == LF || c == CR || c == LS || c == PS;
  }
  private boolean isWhiteSpace(char c) {
    switch (c) {
      case 0x0009:
      case 0x000A:
      case 0x000B:
      case 0x000C:
      case 0x000D:
      case 0x0020:
      case 0x0085:
      case 0x00A0:
      case 0x1680:
      case 0x2000:
      case 0x2001:
      case 0x2002:
      case 0x2003:
      case 0x2004:
      case 0x2005:
      case 0x2006:
      case 0x2007:
      case 0x2008:
      case 0x2009:
      case 0x200A:
      case 0x2028:
      case 0x2029:
      case 0x202F:
      case 0x205F:
      case 0x3000:
        return true;
      default: return false;
    }
  }
  private boolean isHexadecimalChar(char c) {
    if (c >= '0' && c <= '9') {
      return true;
    }
    if (c >= 'A' && c <= 'F') {
      return true;
    }
    if (c >= 'a' && c <= 'f') {
      return true;
    }
    return false;
  }

  /**
   * Transform character to string for log
   */
  private String charToLog(char c) {
    switch (c) {
      case '\b': return "\\b";
      case '\f': return "\\f";
      case '\n': return "\\n";
      case '\r': return "\\r";
      case '\t': return "\\t";
      case '\'': return "'";
      case '"': return "\"";
      case LS: return "LS (0x2028)";
      case PS: return "PS (0x2029)";

      case 0: return "null (0x0000)";

      default: return String.valueOf(c);
    }

  }

  private Map<String, Object> getCollectionForObject(Path path) {
    Map<String, Object> result = null;
    if (listener != null) {
      result = listener.forObject(path.getName());
    }
    if (result == null) {
      result = new LinkedHashMap<>();
    }
    return result;
  }

  private Collection getCollectionForList(Path path) {
    Collection result = null;
    if (listener != null) {
      result = listener.forList(path.getName());
    }
    if (result == null) {
      result = new ArrayList();
    }
    return result;
  }

}
