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

import static js.base.Tools.*;

import java.util.*;

import js.data.DataUtil;
import js.json.JSList;
import js.json.JSMap;

public final class DFA {

  public static final int UNKNOWN_TOKEN = -1;

  DFA() {
  }


  @Deprecated
  public DFA(String script) {
    constructFromJson(new JSMap(script));
  }

  public DFA(JSMap json) {
    constructFromJson(json);
  }

  private void constructFromJson(JSMap map) {
    State.bumpDebugIds();
    if (map.getDouble("version") < 3.0)
      throw badArg("unsupported version:", map.prettyPrint());

    int finalStateIndex = map.getInt("final");
    mTokenNames = map.getList("tokens").asStringArray();
    for (int i = 0; i < mTokenNames.length; i++)
      mTokenNameIdMap.put(mTokenNames[i], i);
    JSList stateInfo = map.getList("states");

    mStates = new State[stateInfo.size()];
    for (int i = 0; i < mStates.length; i++)
      mStates[i] = new State(i == finalStateIndex, null);

    List<Edge> compiledEdges = arrayList();
    int stateId;
    for (stateId = 0; stateId < mStates.length; stateId++) {
      State s = mStates[stateId];
      JSList edges = stateInfo.get(stateId);
      compiledEdges.clear();
      int cursorMax = edges.size();
      int cursor = 0;
      while (cursor < cursorMax) {
        JSList edgeInfo;
        Object element = edges.getUnsafe(cursor);
        if (element instanceof JSList) {
          edgeInfo = (JSList) element;
        } else {
          edgeInfo = list().addUnsafe(element);
        }
        int[] codeSetList = buildCodeSet(edgeInfo);
        cursor++;

        int destStateIndex = finalStateIndex;
        if (cursor < cursorMax) {
          destStateIndex = edges.getInt(cursor);
          cursor++;
        }
        compiledEdges.add(new Edge(codeSetList, getState(destStateIndex)));
      }
      s.setEdges(compiledEdges);
      mStates[stateId] = s;
    }
  }

  /**
   * Get name of token, given its id. Returns "<UNKNOWN>" if its id is
   * UNKNOWN_TOKEN, or "<EOF>" if the id is nil Otherwise, assumes tokenId is
   * 0...n-1
   */
  public String tokenName(int tokenId) {
    if (tokenId == UNKNOWN_TOKEN)
      return "<UNKNOWN>";
    return mTokenNames[tokenId];
  }

  public Integer optTokenId(String tokenName) {
    return mTokenNameIdMap.get(tokenName);
  }

  public Integer tokenId(String tokenName) {
    Integer i = optTokenId(tokenName);
    if (i == null)
      badArg("token name not found:", tokenName);
    return i;
  }

  /**
   * Given a space-delimeted string of token names, construct a set of their
   * indexes
   */
  @Deprecated
  public TreeSet<Integer> parseTokenNames(String spaceDelimitedTokenNames) {
    TreeSet<Integer> set = treeSet();
    for (String name : split(spaceDelimitedTokenNames, ' ')) {
      Integer id = tokenId(name);
      if (id == null)
        throw new IllegalArgumentException("no token found:" + name);
      set.add(id);
    }
    return set;
  }

  /**
   * Build a code range (an array of 2n integers) from a list of integers
   */
  private static int[] buildCodeSet(JSList list) {
    List<Integer> codeSetList = arrayList();
    int elementsLength = list.size();
    int cursor = 0;
    while (cursor < elementsLength) {
      int a, b;
      Object value = list.getUnsafe(cursor);
      cursor++;
      if (!(value instanceof Double)) {
        a = ((Number) value).intValue();
        b = list.getInt(cursor);
        cursor++;
        if (b == 0)
          b = State.CODEMAX;
      } else {
        a = ((Number) value).intValue();
        b = a + 1;
      }
      codeSetList.add(a);
      codeSetList.add(b);
    }
    return DataUtil.intArray(codeSetList);
  }

  public String[] tokenNames() {
    return mTokenNames;
  }

  public State getStartState() {
    return mStates[0];
  }

  private State getState(int id) {
    return mStates[id];
  }

  private String[] mTokenNames;
  private Map<String, Integer> mTokenNameIdMap = hashMap();
  private State[] mStates;


  private static class SimpleScanner {
    CharSequence text;
    int cursor;

    SimpleScanner(CharSequence text) {
      this.text = text;
    }

    char peek() {
      while (true) {
        if (cursor == text.length()) return 0;
        var c = text.charAt(cursor);
        if (c == ' ') {
          cursor++;
        } else {
          return c;
        }
      }
    }

    boolean readIf(char ch) {
      if (peek() == ch) {
        read();
        return true;
      }
      return false;
    }

    int readInt() {
      int curs = this.cursor;
      while (true) {
        var c = peek();
        if (c == '-' || (c >= '0' && c <= '9')) {
          read();
          continue;
        }
        break;
      }
      var tx = text.subSequence(curs, this.cursor);
      skipComma();
      return Integer.parseInt(tx.toString());
    }

    void readExp(char ch) {
      var ch2 = read();
      if (ch != ch2)
        badArg("unexpected character:", ch2, "; was expecting:", ch);
    }

    char read() {
      var x = peek();
      checkArgument(x != 0, "unexpected EOF");
      this.cursor++;
      return x;
    }

    String readStr() {
      readExp('"');
      var sb = new StringBuilder();
      while (true) {
        if (readIf('"')) {
          skipComma();
          break;
        }
        sb.append(read());
      }
      return sb.toString();
    }

    String readPhrase(String endTokens) {
      var s = new StringBuilder();
      int nestLevel = 0;
      while (true) {
        var ch = peek();
        if (ch == '[' || ch == '{') {
          nestLevel++;
        } else if (ch == ']' || ch == '}') {
          if (nestLevel == 0) break;
          nestLevel--;
        }
        if (nestLevel == 0 && endTokens.indexOf(ch) >= 0) {
          break;
        }
        s.append(read());
      }
      skipComma();
      return s.toString();
    }

    void skipComma() {
      while (readIf(',')) ;
    }

  }

  /**
   * A bespoke parser for json.dfa, which is simpler than general
   * json: only a single outer map {...}, no floating point numbers, and
   * no strings that need escaping.  This allows us to bootstrap to construct
   * a DFA to parse (more elaborate) json content
   */
  public static DFA parseDfaFromJson(String source) {
    State.bumpDebugIds();

    var dfa = new DFA();
    List<State> states = arrayList();

    // Parse the outer map {....} into a map of keys and values that are
    // json subexpressions that will need to be parsed separately.

    Map<String, String> mp = hashMap();

    {
      var scanner = new SimpleScanner(source);

      scanner.readExp('{');
      while (true) {
        if (scanner.readIf('}')) break;
        var key = scanner.readStr();
        scanner.readExp(':');
        var value = scanner.readPhrase(",}");
        mp.put(key, value);
      }
    }

    // Validate the version number, and final state index
    checkArgument(mp.get("version").equals("4.0"), "unexpected version");

    var finalStateIndex = Integer.parseInt(mp.get("final"));

    // Parse the token names
    {
      var scanner = new SimpleScanner(mp.get("tokens"));
      List<String> tokenNames = arrayList();
      while (scanner.peek() != 0) {
        var ch = scanner.read();
        if (ch == '"') {
          tokenNames.add(scanner.readPhrase("\""));
          scanner.readExp('"');
        }
      }
      dfa.mTokenNames = tokenNames.toArray(new String[0]);
    }

    // Parse the states.
    //
    // This would be quite a bit simpler if we knew the number of states in advance,
    // so we could pre-construct the required number of states in advance...
    {
      var scanner = new SimpleScanner(mp.get("states"));
      scanner.readExp('['); // open list of states
      int stateNumber = 0;

      while (true) {
        scanner.skipComma();
        if (scanner.readIf(']')) {
          scanner.skipComma();
          break;
        }
        scanner.readExp('['); // open a state

        var currentState = extendStates(states, stateNumber);

        // Loop over edges
        while (true) {
          scanner.skipComma();
          if (scanner.readIf(']')) break;
          // Loop over edge info
          scanner.readExp('[');
          List<Integer> codeSets = arrayList();
          while (!scanner.readIf(']')) {
            codeSets.add(scanner.readInt());
            scanner.skipComma();
          }
          scanner.skipComma();
          int targetState = finalStateIndex;
          // If there's another number, it's the target state index; otherwise, the target state is the end state
          if (scanner.peek() != ']') {
            targetState = scanner.readInt();
            scanner.skipComma();
          }

          var targetStatePtr = extendStates(states, targetState);

          int[] ia = new int[codeSets.size()];
          int i = INIT_INDEX;
          for (Integer x : codeSets) {
            i++;
            ia[i] = x;
          }
          var edge = new Edge(ia, targetStatePtr);
          currentState.edges().add(edge);
        }
        stateNumber++;
      }
      states.get(finalStateIndex).setFinal(true);
      dfa.mStates = states.toArray(new State[0]);
    }
    return dfa;
  }

  // Extend a list of states to accomodate a particular state number (if necessary);
  // return that state
  //
  private static State extendStates(List<State> states, int stateNumber) {
    while (stateNumber >= states.size())
      states.add(new State());
    return states.get(stateNumber);
  }

  // Quick test
  public static void main(String[] args) {
    pr("hello");

    String text = "{\"final\":2,\"tokens\":[\"CR\",\"COMMA\",\"VALUE\"],\"version\":4.0,\"states\":[[[33,34,35,44,45,92,93,128],10,[44,45],9,[34,35],5,[32,33],4,[13,14],3,[10,11],1],[[-2,-1]],[],[[10,11],1],[[33,34,35,44,45,92,93,128],10,[-4,-3],2,[34,35],5,[32,33],4],[[32,34,35,92,93,255],5,[92,93],7,[34,35],6],[[-4,-3],2,[32,33],6],[[32,34,35,92,93,255],5,[92,93],7,[34,35],8],[[-4,-3],2,[33,34,35,92,93,255],5,[92,93],7,[32,33],8,[34,35],6],[[-3,-2]],[[32,34,35,44,45,92,93,128],10,[-4,-3]]]}";

    var mp = new JSMap(text);
    pr(mp);

    var dfa = new DFA(mp);
    show("from JSMap", dfa);
    var dfa2 = parseDfaFromJson(text);
    show("from string", dfa2);
  }

  private static void show(String prompt, DFA d) {
    pr("dfa:", prompt);
    pr("start state", d.getStartState());
    for (var s : d.mStates) {
      pr(s.toString(true));
    }
    var sampleText = "123,45,\"hello\",\n16";

    var sc = new Scanner(d, sampleText, -1);
    while (sc.hasNext()) {
      pr("next token:", sc.read());
    }
  }
}
