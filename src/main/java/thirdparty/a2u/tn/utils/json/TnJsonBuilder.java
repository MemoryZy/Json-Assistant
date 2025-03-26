/**
 * Original code modified from tnjson (Unlicense License).
 * Source: https://github.com/anymaker/tnjson
 * Original package: a2u.tn.utils.json
 */
package thirdparty.a2u.tn.utils.json;


/**
 * json generation configurator
 */
public class TnJsonBuilder extends TnJson {
  boolean isKeyQuoted = true;
  char quoteSymbol = '"';
  boolean isFormated = false;
  boolean isStayReadable = false;
  boolean isAllowMultiRowString = false;
  boolean isKeepNull = false;


  /**
   * Interfase for handing generating json in concrete path
   */
  public interface IPathHandler {
    /**
     * This methow will be called when json generator start serialize new value.<br>
     * @param path path from root element witn dot as separator sample ".num", ".internal.cls"
     * @param value value in this path
     * @return the json-string or other value which be converted to json by standard rules
     */
    Object handlePath(String path, Object value);
  }

  IPathHandler pathHandler = null;


  /**
   * Interface for handing generating json by concrete type
   */
  public interface ITypeHandler {
    /**
     * This methow will be called when json generator start serialize new value.<br>
     * @param value not null value for serialization
     * @return the json-string or other value which be converted to json by standard rules
     */
    Object handleType(Object value);
  }

  ITypeHandler typeHandler = null;



  TnJsonBuilder() {
    //hide constructor
  }



  /**
   * Create new builder
   * @return new object with default settings
   */
  static TnJsonBuilder init() {
    return new TnJsonBuilder();
  }

  /**
   * Disable generating quotes in the key
   * @return this builder
   */
  public TnJsonBuilder withoutKeyQuote() {
    isKeyQuoted = false;
    return this;
  }

  /**
   * Use single quotes
   * @return this builder
   */
  public TnJsonBuilder singleQuote() {
    quoteSymbol = '\'';
    return this;
  }

  /**
   * Format the final json
   * @return this builder
   */
  public TnJsonBuilder formated() {
    isFormated = true;
    return this;
  }

  /**
   * Leave characters in a strings as readable as possible
   * @return this builder
   */
  public TnJsonBuilder readable() {
    isStayReadable = true;
    return this;
  }

  /**
   * Allow linefeed in a strings
   * @return this builder
   */
  public TnJsonBuilder allowMultiRowString() {
    isAllowMultiRowString = true;
    return this;
  }

  /**
   * Allow null values
   * @return this builder
   */
  public TnJsonBuilder keepNull() {
    isKeepNull = true;
    return this;
  }

  /**
   * Set handler for generating json in concrete path
   * @param pathHandler handler @see IPathHandler
   * @return this builder
   */
  public TnJsonBuilder handlePath(IPathHandler pathHandler) {
    this.pathHandler = pathHandler;
    return this;
  }

  /**
   * Set handler for generating json by concrete type
   * @param typeHandler handler @see ITypeHandler
   * @return this builder
   */
  public TnJsonBuilder handleType(ITypeHandler typeHandler) {
    this.typeHandler = typeHandler;
    return this;
  }


  /**
   * Building JSON by parameters in this builder
   * @param data Object to convert. It can be Map, Collection, array, or any other object
   * @return JSON-string
   */
  @Override
  public String buildJson(Object data) {
    return JsonSerializer.toJson(data, this);
  }

}
