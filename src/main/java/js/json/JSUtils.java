/**
 * MIT License
 * 
 * Copyright (c) 2021 Jeff Sember
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
package js.json;

import java.util.List;
import java.util.Map;

public final class JSUtils {

  /**
   * Parse a json value from a character sequence
   *
   * @throws IllegalArgumentException
   *           if bad format in input
   */
  @SuppressWarnings("unchecked")
  public static <T> T parse(CharSequence s) {
    JSParser p = new JSParser(s);
    Object value = p.readValue();
    p.assertCompleted();
    return (T) value;
  }

  static void printAsQuotedJsonString(CharSequence sourceSequence, StringBuilder destStringBuilder) {
    destStringBuilder.append('"');
    for (int i = 0; i < sourceSequence.length(); i++) {
      char c = sourceSequence.charAt(i);
      final char ESCAPE = '\\';
      switch (c) {
      case '"':
        destStringBuilder.append(ESCAPE);
        break;
      case ESCAPE:
        destStringBuilder.append(ESCAPE);
        break;
      case 8:
        destStringBuilder.append(ESCAPE);
        c = 'b';
        break;
      case 12:
        destStringBuilder.append(ESCAPE);
        c = 'f';
        break;
      case 10:
        destStringBuilder.append(ESCAPE);
        c = 'n';
        break;
      case 13:
        destStringBuilder.append(ESCAPE);
        c = 'r';
        break;
      case 9:
        destStringBuilder.append(ESCAPE);
        c = 't';
        break;
      default:
        if (c < ' ' || c > 126) {
          destStringBuilder.append("\\u");
          toHex(destStringBuilder, (int) c, 4);
          continue;
        }
        break;
      }
      destStringBuilder.append(c);
    }
    destStringBuilder.append('"');
  }

  /**
   * Convert a Java object to a json value. This is not particularly efficient,
   * and should be used accordingly.
   * <p>
   * If type of object cannot be associated easily with a json type (e.g. Map =>
   * JSMap), it returns value.toString()
   */
  static Object asJson(Object value) {
    if (value == null)
      return value;
    if (value instanceof CharSequence) {
      CharSequence c = (CharSequence) value;
      int i = 0;
      while (i < c.length() && c.charAt(i) <= ' ')
        i++;
      if (i < c.length()) {
        char ch = c.charAt(i);
        if (ch == '{')
          try {
            return new JSMap(c);
          } catch (Throwable t1) {
          }

        if (ch == '[')
          try {
            return new JSList(c);
          } catch (Throwable t2) {
          }
      }
      return c;
    }
    if (value instanceof Number)
      return value;
    if (value instanceof Boolean)
      return value;
    if (value instanceof Map)
      return toMap((Map) value);
    if (value instanceof List)
      return JSList.withUnsafeList((List) value);
    if (value instanceof JSObject)
      return value;
    return value.toString();
  }

  private static JSMap toMap(Map map) {
    JSMap jsMap = new JSMap();
    @SuppressWarnings("unchecked")
    Map<Object, Object> map2 = (Map<Object, Object>) map;
    for (Map.Entry<Object, Object> entry : map2.entrySet()) {
      String key = entry.getKey().toString();
      Object jsonValue = asJson(entry.getValue());
      jsMap.putUnsafe(key, jsonValue);
    }
    return jsMap;
  }

  static Object deepCopy(Object value) {
    if (value != null) {
      if (value instanceof JSObject) {
        value = ((JSObject) value).deepCopy();
      }
    }
    return value;
  }

  static void printValueTo(Object value, StringBuilder sb, int indent) {
    if (value == null)
      sb.append("null");
    else if (value instanceof JSObject)
      ((JSObject) value).printTo(sb, indent);
    else if (value instanceof String)
      printAsQuotedJsonString((String) value, sb);
    else
      sb.append(value);
  }

  /**
   * Convert value to hex, store in StringBuilder
   */
  private static void toHex(StringBuilder stringBuilder, int value, int digits) {
    while (digits-- > 0) {
      int shift = digits << 2;
      int v = (value >> shift) & 0xf;
      char c;
      if (v < 10) {
        c = (char) ('0' + v);
      } else {
        c = (char) ('a' + (v - 10));
      }
      stringBuilder.append(c);
    }
  }
}
