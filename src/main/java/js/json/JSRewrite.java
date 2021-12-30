package js.json;

import static js.base.Tools.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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

}
