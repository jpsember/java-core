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
 **/
package js.json;

import java.util.List;
import java.util.Map;

import js.data.DataUtil;
import js.parsing.DFA;
import js.parsing.Scanner;

import static js.base.Tools.*;

public final class JSUtils {

  public static final DFA JSON_DFA = DFA.parse("{\"graph\":[47,16,1,125,126,417,1,123,124,413,1,116,117," +
      "394,1,110,111,375,1,102,103,351,1,93,94,347,1,91,92,343,1,58,59,339,1,49,58,321,1,48,49,264,1,47,48," +
      "213,1,45,46,204,1,44,45,200,1,35,36,141,1,34,35,83,3,9,11,12,14,32,33,70,2,1,-1,82,3,9,11,12,14,32,33," +
      "70,0,3,3,32,34,35,92,93,256,83,1,92,93,104,1,34,35,100,1,1,-11,82,3,3,32,34,35,92,93,256,83,1,92,93,104," +
      "1,34,35,121,4,1,-11,82,3,32,34,35,92,93,256,83,1,92,93,104,1,34,35,100,4,1,-1,82,1,10,11,181,3,1,10,11," +
      "13,14,256,141,1,13,14,161,1,1,47,48,166,1,1,47,48,171,1,1,13,14,176,1,1,10,11,181,1,1,47,48,186,1,1,47," +
      "48,191,1,1,10,11,196,1,1,-1,82,1,1,-9,82,2,1,49,58,321,1,48,49,264,2,1,47,48,250,1,42,43,222,2,2,1,42," +
      "43,256,222,1,42,43,233,3,1,47,48,196,3,1,42,43,47,48,256,222,1,42,43,233,3,1,-1,82,2,1,10,11,256,250,1," +
      "10,11,181,3,1,-12,82,2,69,70,101,102,297,1,46,47,278,1,1,48,58,283,3,1,-12,82,2,69,70,101,102,297,1,48," +
      "58,283,2,1,48,58,313,2,43,44,45,46,308,1,1,48,58,313,2,1,-12,82,1,48,58,313,4,1,-12,82,1,48,58,321,2,69," +
      "70,101,102,297,1,46,47,278,1,1,-10,82,1,1,-2,82,1,1,-3,82,1,1,97,98,356,1,1,108,109,361,1,1,115,116,366," +
      "1,1,101,102,371,1,1,-5,82,1,1,117,118,380,1,1,108,109,385,1,1,108,109,390,1,1,-6,82,1,1,114,115,399,1,1," +
      "117,118,404,1,1,101,102,409,1,1,-4,82,1,1,-7,82,1,1,-8,82]," +
      "\"token_names\":\"WS BROP BRCL TRUE FALSE NULL CBROP CBRCL COMMA COLON STRING NUMBER\",\"version\":\"$1\"}\n");

  static void printAsQuotedJsonString(CharSequence sourceSequence, StringBuilder sb) {
    DataUtil.escapeChars(sourceSequence, sb, true);
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

  public static String valueToString(Object value) {
    StringBuilder sb = new StringBuilder();
    printValueTo(value, sb, 0);
    return sb.toString();
  }

  static Object parseValue(Scanner s) {
    var t = s.peek();
    checkNotNull(t, "expected token");
    switch (t.id()) {
      default:
        throw badArg("unexpected token:", t);
      case J_BROP:
        return JSList.parseFrom(s, null);
      case J_CBROP:
        return JSMap.parseFrom(s, null);
      case J_STRING:
        return parseStringFrom(s.read().text());
      case J_TRUE:
      case J_FALSE:
        return s.read().id(J_TRUE);
      case J_NULL:
        s.read();
        return null;
      case J_NUMBER:
        return parseNumberFrom(s.read().text());
    }
  }

  static String parseStringFrom(String s) {
    StringBuilder w = new StringBuilder();
    int cursor = 1;
    int cursorMax = s.length() - 1;
    while (cursor < cursorMax) {
      char c = s.charAt(cursor++);
      if (c != '\\') {
        w.append(c);
        continue;
      }
      c = s.charAt(cursor++);
      switch (c) {
        case '\\':
        case '"':
        case '/':
          w.append(c);
          break;
        case 'b':
          w.append('\b');
          break;
        case 'f':
          w.append('\f');
          break;
        case 'n':
          w.append('\n');
          break;
        case 'r':
          w.append('\r');
          break;
        case 't':
          w.append('\t');
          break;
        case 'u':
          if (cursor + 4 > cursorMax)
            throw badArg("illegal escape sequence:", s);
          w.append((char) ((readHex(s, c) << 12) | (readHex(s, c + 1) << 8) | (readHex(s, c + 2) << 4) | readHex(s, c + 3)));
          cursor += 4;
          break;
        default:
          throw badArg("illegal escape sequence:", s);
      }
    }
    return w.toString();
  }

  private static int readHex(
      String s, int cursor) {
    var val = (int) s.charAt(cursor);
    if (val >= '0' && val <= '9')
      return val - '0';
    if (val >= 'A' && val <= 'F')
      return val - 'A' + 10;
    if (val >= 'a' && val <= 'f')
      return val - 'a' + 10;
    throw badArg("not a hex digit:", s);
  }

  private static Number parseNumberFrom(String text) {
    try {
      var dblValue = Double.parseDouble(text);
      var longValue = (long) dblValue;
      if (dblValue == longValue) {
        return longValue;
      }
      return dblValue;
    } catch (NumberFormatException e) {
      throw badArg("trouble parsing number:", text);
    }
  }

  // Token Ids generated by 'dev dfa' tool (DO NOT EDIT BELOW)
  static final int J_WS = 0;
  static final int J_BROP = 1;
  static final int J_BRCL = 2;
  static final int J_TRUE = 3;
  static final int J_FALSE = 4;
  static final int J_NULL = 5;
  static final int J_CBROP = 6;
  static final int J_CBRCL = 7;
  static final int J_COMMA = 8;
  static final int J_COLON = 9;
  static final int J_STRING = 10;
  static final int J_NUMBER = 11;
  // End of token Ids generated by 'dev dfa' tool (DO NOT EDIT ABOVE)

}
