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

import js.data.ShortArray;
import js.json.JSList;
import js.json.JSMap;
import js.parsing.Token;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static js.base.Tools.*;

/**
 * A new, more compact representation of a DFA
 */

// {
//   "version" : "$1",
//   "tokens" : "space-delimited set of token names",
//   "graph"  : [ array of integers, described below ]
// }
//
// <graph> ::= <int: # of states> <state>*
//
// <state> ::= <edge count> <edge>*
//
// <edge>  ::= <int: number of char_range items> <char_range>* <dest_state_id>
//
// <char_range> ::= <int: start of range> <int: end of range (exclusive)>
//                | <int: -(token index + 1)>
//
// <dest_state_id> ::= offset of state within graph
//
public class DFA {

  public static String VERSION = "$1";

  public DFA(String version, String[] tokenNames, short[] graph) {
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

  public short[] graph() {
    return mGraph;
  }

  public String tokenName(int id) {
    if (id == Token.ID_UNKNOWN)
      return "<UNKNOWN>";
    return mTokenNames[id];
  }

  @Deprecated // suspect it's unused
  public String[] tokenNames() {
    return mTokenNames;
  }

  private String mVersion;
  private String[] mTokenNames;
  private short[] mGraph;


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
   * 3)  ('A'..'Z', '_')+
   * => token id
   */
  public static DFA parse(String str) {

    // Set default values
    //
    String version = VERSION;
    List<String> tokenNames = arrayList();
    var nums = ShortArray.DEFAULT_INSTANCE.newBuilder();

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
          if (!isUpper(strBytes[i])) {
            tokenNames.add(new String(strBytes, j, i - j));
            break;
          }
        }
      } else if (isNumber(b)) {
        while (true) {
          i++;
          if (!isNumber(strBytes[i])) {
            nums.add(Short.parseShort(new String(strBytes, j, i - j)));
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
    return b == '_' || (b >= 'A' && b <= 'Z');
  }

  private static boolean isNumber(byte b) {
    return (b >= '0' && b <= '9') || b == '-';
  }

  @Deprecated // suspect it's unused
  public int numTokens() {
    return mTokenNames.length;
  }
}
