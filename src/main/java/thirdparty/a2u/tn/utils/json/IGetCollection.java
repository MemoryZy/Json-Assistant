/**
 * Original code modified from tnjson (Unlicense License).
 * Source: https://github.com/anymaker/tnjson
 * Original package: a2u.tn.utils.json
 */
package thirdparty.a2u.tn.utils.json;

import java.util.Collection;
import java.util.Map;

/**
 * The callback-interface for specifying type of collection which will be returned when parsing.
 * @see TnJson#parse(String, IGetCollection)
 */
public interface IGetCollection {

  /**
   * Name of root element in a path. Using in call-back methods of IGetCollection.
   * @see #forObject(String)
   * @see #forList(String)
   */
  public static final String PATH_ROOT_KEY = JsonParser.PATH_ROOT_KEY;


  /**
   * This methow will be called when parcer need create new map.<br>
   * If this method returns null, then map will be created with the default type - LinkedHashMap.
   *
   * @param path path of current element, starting from root. If this a root element, then path equal "root".
   * @return the empty object who implementing interface from java.util.Map&lt;String, Object&gt;.
   *
   * @see #PATH_ROOT_KEY
   */
  Map<String, Object> forObject(String path);

  /**
   * This methow will be called when parcer need create new array.<br>
   * If this method returns null, then array will be created with the default type - ArrayList.
   *
   * @param path path of current element, starting from root. If this a root element, then path equal "root".
   * @return the empty object who implementing interface from java.util.Collection&lt;Object&gt;.
   *
   * @see #PATH_ROOT_KEY
   */
  Collection forList(String path);
}
