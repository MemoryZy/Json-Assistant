/**
 * Original code modified from tnjson (Unlicense License).
 * Source: https://github.com/anymaker/tnjson
 * Original package: a2u.tn.utils.json
 */
package thirdparty.a2u.tn.utils.json;

/**
 * Exception on serialization error
 */
public class SerializeException extends RuntimeException {

  public SerializeException(String s) {
    super(s);
  }

  public SerializeException(String s, Throwable cause) {
    super(s, cause);
  }

}