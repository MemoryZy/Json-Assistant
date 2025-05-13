/**
 * Original code modified from tnjson (Unlicense License).
 * Source: https://github.com/anymaker/tnjson
 * Original package: a2u.tn.utils.json
 */
package thirdparty.a2u.tn.utils.json;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Navigate by Map represented object.<br>
 * Every value in the Map must be
 * <ul>
 *   <li>either a simple value: string, number, boolean, or other;</li>
 *   <li>either by a Map such as Map&lt;String, Object&gt;;</li>
 *   <li>either an array of values such as Collection&lt;Object&gt;.</li>
 * </ul>
 */
public class MapNavigator {

  private MapNavigator() {
    //hide this
  }


  /**
   * Find value from map by path.
   * @param map map with values
   * @param path path with string-keys and dot as separator
   * @return value
   */
  public static Object fromPath(Map map, String path) {

    Object res = map;
    List<String> pathList =  Arrays.asList(path.split("\\."));

    StringBuilder node = new StringBuilder();

    for (String pat : pathList) {
      if (res == null) {
        return null;
      }
      if (res instanceof Map) {
        res = ((Map) res).get(pat);
      }
      else if (res instanceof List) {
        int ix = Integer.parseInt(pat);
        res = ((List) res).get(ix);
      }
      else {
        throw new RuntimeException("Incorrect path: node=" + node.toString() + " is not a Map or List.");
      }
      node.append(".").append(pat);
    }

    return res;
  }
}
