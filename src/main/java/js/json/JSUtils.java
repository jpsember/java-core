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


  private static final DFA JsonDfa = DFA.parseDfaFromJson(
      "{\"final\":2,\"tokens\":[\"WS\",\"BROP\",\"BRCL\",\"TRUE\",\"FALSE\",\"NULL\",\"CBROP\",\"CBRCL\",\"COMMA\",\"COLON\",\"STRING\",\"NUMBER\"],\"version\":4.0,\"states\":[[[125,126],47,[123,124],46,[116,117],42,[110,111],38,[102,103],33,[93,94],32,[91,92],31,[58,59],29,[49,58],28,[48,49],22,[47,48],18,[45,46],17,[44,45],16,[35,36],7,[34,35],3,[9,11,12,14,32,33],1],[[-2,-1],2,[9,11,12,14,32,33],1],[],[[32,34,35,92,93,256],3,[92,93],5,[34,35],4],[[-12,-11]],[[32,34,35,92,93,256],3,[92,93],5,[34,35],6],[[-12,-11],2,[32,34,35,92,93,256],3,[92,93],5,[34,35],4],[[-2,-1],2,[10,11],12,[1,10,11,13,14,256],7,[13,14],8],[[47,48],9],[[47,48],10],[[13,14],11],[[10,11],12],[[47,48],13],[[47,48],14],[[10,11],15],[[-2,-1]],[[-10,-9]],[[49,58],28,[48,49],22],[[47,48],21,[42,43],19],[[1,42,43,256],19,[42,43],20],[[47,48],15,[1,42,43,47,48,256],19,[42,43],20],[[-2,-1],2,[1,10,11,256],21,[10,11],12],[[-13,-12],2,[69,70,101,102],25,[46,47],23],[[48,58],24],[[-13,-12],2,[69,70,101,102],25,[48,58],24],[[48,58],27,[43,44,45,46],26],[[48,58],27],[[-13,-12],2,[48,58],27],[[-13,-12],2,[48,58],28,[69,70,101,102],25,[46,47],23],[[44,45],30],[[-11,-10]],[[-3,-2]],[[-4,-3]],[[97,98],34],[[108,109],35],[[115,116],36],[[101,102],37],[[-6,-5]],[[117,118],39],[[108,109],40],[[108,109],41],[[-7,-6]],[[114,115],43],[[117,118],44],[[101,102],45],[[-5,-4]],[[-8,-7]],[[-9,-8]]]}"
  );

  /**
   * Parse a json value from a character sequence
   *
   * @throws IllegalArgumentException if bad format in input
   */
  public static <T> T parse(CharSequence s) {
    JSParser p = new JSParser(s);
    Object value = p.readValue();
    p.assertCompleted();
    return (T) value;
  }

  /**
   * Parse a json value from a character sequence
   *
   * @throws IllegalArgumentException if bad format in input
   */
  public static JSMap parseNew(String s) {
    var scanner = new Scanner(JsonDfa, s);
    scanner.setVerbose(true);
    return JSMap.parseFrom(scanner);
  }

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
        return JSList.parseFrom(s);
      case J_CBROP:
        return JSMap.parseFrom(s);
      case J_STRING:
        return parseStringFrom(s.read().text());
      case J_TRUE:
      case J_FALSE:
        return s.read().id(J_TRUE);
      case J_NULL:
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
