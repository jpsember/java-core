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
package js.parsing;

import js.data.ByteArray;
import js.json.JSList;
import js.json.JSMap;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static js.base.Tools.*;

/**
 * An even newer, more compact representation of a DFA
 */

// {
//   "version" : "$2",
//   "tokens" : "space-delimited set of token names",
//   "graph"  : [ array of bytes, described below ]
// }
//
// <graph> ::= <state>*
//
// <state> ::= <1 + token id, or 0> <edge count> <edge>*
//
// <edge>  ::= <char_range count> <char_range>* <dest_state_offset, low byte first>
//
// <char_range> ::= <start of range (1..127)> <size of range>
//
//
public class DFA {

  public static String VERSION = "$2";

  public DFA(String version, String[] tokenNames, byte[] graph) {
    checkArgument(version.equals(VERSION), "bad version:", version, "; expected", VERSION);
    mVersion = version;
    mTokenNames = tokenNames;
    mGraph = graph;
  }

  public JSMap toJson() {
    var m = map();
    m.put("version", mVersion);
    m.put("token_names", String.join(" ", mTokenNames));
    m.put("graph", JSList.with(mGraph));
    return m;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public byte[] graph() {
    return mGraph;
  }

  public String tokenName(int id) {
    if (id == Token.ID_UNKNOWN)
      return "<UNKNOWN>";
    return mTokenNames[id];
  }

  public String[] tokenNames() {
    return mTokenNames;
  }

  private String mVersion;
  private String[] mTokenNames;
  private byte[] mGraph;


  // ----------------------------------------------------------------------------------------------
  // Parsing from a string
  // ----------------------------------------------------------------------------------------------

  /**
   * Parse a DFA from a string.
   *
   * It looks within the string (which might be a JSMap) for these fields:
   *
   * 1)  ( '0'...'9' '-')+
   * => an integer within the graph
   * 2)  '$' (any character except '$')*
   * => version (e.g. $42)
   * 3)  ('A'..'Z') ('A'..'Z', '_')*
   * => token id
   */
  public static DFA parse(String str) {

    // Set default values
    //
    String version = VERSION;
    List<String> tokenNames = arrayList();
    var nums = ByteArray.newBuilder();

    var strBytes = str.getBytes(StandardCharsets.UTF_8);
    var i = 0;
    while (i < strBytes.length) {
      var j = i;
      var b = strBytes[i];
      if (b == '$') {
        while (true) {
          i++;
          b = strBytes[i];
          if (b == '"') {
            version = new String(strBytes, j, i - j);
            break;
          }
        }
      } else if (isUpper(b)) {
        while (true) {
          i++;
          var by = strBytes[i];
          if (!(isUpper(by) || by == '_')) {
            tokenNames.add(new String(strBytes, j, i - j));
            break;
          }
        }
      } else if (isNumber(b)) {
        while (true) {
          i++;
          if (!isNumber(strBytes[i])) {
            nums.add(Byte.parseByte(new String(strBytes, j, i - j)));
            break;
          }
        }
      } else {
        i++;
      }
    }
    return new DFA(version, tokenNames.toArray(new String[0]), nums.array());
  }

  // Parser helper functions

  private static boolean isUpper(byte b) {
    return b >= 'A' && b <= 'Z';
  }

  private static boolean isNumber(byte b) {
    return (b >= '0' && b <= '9') || b == '-';
  }

}
