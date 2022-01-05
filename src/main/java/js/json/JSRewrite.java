package js.json;

import static js.base.Tools.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import js.data.DataUtil;

public class JSRewrite {

  /**
   * Rewrite a JSMap
   * 
   * Default implementation rewrites the values, deleting keys whose rewritten
   * values are null
   */
  public Object rewrite(JSMap original) {
    Map<String, Object> target = new ConcurrentHashMap<>();
    for (Entry<String, Object> entry : original.wrappedMap().entrySet()) {
      Object convertedValue = rewriteObject(entry.getValue());
      if (convertedValue != null)
        target.put(entry.getKey(), convertedValue);
    }
    return new JSMap(target);
  }

  /**
   * Rewrite a JSList
   * 
   * Default implementation rewrites each of the list's values, omitting null
   * values
   */
  public Object rewrite(JSList original) {
    List<Object> rewritten = arrayList();
    for (Object value : original.wrappedList()) {
      Object convertedValue = rewriteObject(value);
      if (convertedValue != null)
        rewritten.add(convertedValue);
    }
    return new JSList(rewritten);
  }

  /**
   * Rewrite a number
   * 
   * Default implementation returns value unchanged
   */
  public Object rewrite(Number number) {
    return number;
  }

  /**
   * Rewrite a string
   * 
   * Default implementation returns value unchanged
   */
  public Object rewrite(String string) {
    return string;
  }

  /**
   * Rewrite a boolean
   * 
   * Default implementation returns value unchanged
   */
  public Object rewrite(Boolean bool) {
    return bool;
  }

  private Object rewriteObject(Object value) {
    if (value instanceof JSObject) {
      if (value instanceof JSMap)
        return rewrite((JSMap) value);
      return rewrite((JSList) value);
    }

    if (value instanceof Number)
      return rewrite((Number) value);

    if (value instanceof String)
      return rewrite((String) value);

    return rewrite((Boolean) value);
  }

  /**
   * A rewriter that converts datagen representations of arrays of primitive
   * values from their Base64 (variant) forms to standard json lists
   */
  public static final JSRewrite CONVERT_DATA_TO_JSON_REWRITER = new JSRewrite() {

    private final char DELIM = DataUtil.DATA_TYPE_DELIMITER.charAt(0);

    @Override
    public Object rewrite(String string) {
      Object output = string;
      if (string.length() >= 2 && string.charAt(string.length() - 2) == DELIM) {
        String suffix = string.substring(string.length() - 2);
        switch (suffix) {
        case DataUtil.DATA_TYPE_SUFFIX_BYTE:
          output = JSList.with(DataUtil.parseBase64(string));
          break;
        case DataUtil.DATA_TYPE_SUFFIX_SHORT:
          output = JSList.with(DataUtil.parseBase64Shorts(string));
          break;
        case DataUtil.DATA_TYPE_SUFFIX_INT:
          output = JSList.with(DataUtil.parseBase64Ints(string));
          break;
        case DataUtil.DATA_TYPE_SUFFIX_LONG:
          output = JSList.with(DataUtil.parseBase64Longs(string));
          break;
        }
      }
      return output;
    }
  };
}
