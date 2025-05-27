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
import js.json.JSUtils;

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

  public State[] debStates() {
    return mStates;
  }

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


  public static DFA parseDfaUsingBespokeParser(String source) {
    return parseDfaFromJson(source);
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

    String text = "{\"final\":2,\"tokens\":[\"WS\",\"BROP\",\"BRCL\",\"TRUE\",\"FALSE\",\"NULL\",\"CBROP\",\"CBRCL\",\"COMMA\",\"COLON\",\"STRING\",\"NUMBER\"],\"version\":4.0,\"states\":[[[125,126],47,[123,124],46,[116,117],42,[110,111],38,[102,103],33,[93,94],32,[91,92],31,[58,59],29,[49,58],28,[48,49],22,[47,48],18,[45,46],17,[44,45],16,[35,36],7,[34,35],3,[9,11,12,14,32,33],1],[[-2,-1],2,[9,11,12,14,32,33],1],[],[[32,34,35,92,93,256],3,[92,93],5,[34,35],4],[[-12,-11]],[[32,34,35,92,93,256],3,[92,93],5,[34,35],6],[[-12,-11],2,[32,34,35,92,93,256],3,[92,93],5,[34,35],4],[[-2,-1],2,[10,11],12,[1,10,11,13,14,256],7,[13,14],8],[[47,48],9],[[47,48],10],[[13,14],11],[[10,11],12],[[47,48],13],[[47,48],14],[[10,11],15],[[-2,-1]],[[-10,-9]],[[49,58],28,[48,49],22],[[47,48],21,[42,43],19],[[1,42,43,256],19,[42,43],20],[[47,48],15,[1,42,43,47,48,256],19,[42,43],20],[[-2,-1],2,[1,10,11,256],21,[10,11],12],[[-13,-12],2,[69,70,101,102],25,[46,47],23],[[48,58],24],[[-13,-12],2,[69,70,101,102],25,[48,58],24],[[48,58],27,[43,44,45,46],26],[[48,58],27],[[-13,-12],2,[48,58],27],[[-13,-12],2,[48,58],28,[69,70,101,102],25,[46,47],23],[[44,45],30],[[-11,-10]],[[-3,-2]],[[-4,-3]],[[97,98],34],[[108,109],35],[[115,116],36],[[101,102],37],[[-6,-5]],[[117,118],39],[[108,109],40],[[108,109],41],[[-7,-6]],[[114,115],43],[[117,118],44],[[101,102],45],[[-5,-4]],[[-8,-7]],[[-9,-8]]]}";

    if (true) {
      var result = validateNewParsing(text);
      pr(result);
      return;
    }
    var mp = new JSMap(text);
    pr(mp);

    var v2 = (JSMap) JSUtils.parseNew(text);
    pr(v2);
    if (true) return;


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

  private static JSMap DFADump(DFA d) {
    var m = map();
    for (var s : d.mStates) {
      var stateKey = "" + s.debugId();
      var altKey = stateKey + "*";

      if (m.containsKey(stateKey) || m.containsKey(altKey)) {
        m.putNumbered(stateKey, "*** duplicate state id ***");
        continue;
      }
      var edgeMap = map();
      m.put(s.finalState() ? altKey : stateKey, edgeMap);
      for (var edge : s.edges()) {
        var edgeKey = "--> " + edge.destinationState().debugId();
        if (edgeMap.containsKey(edgeKey))
          edgeMap.put("**ERR** " + edge.destinationState().debugId(), "duplicate destination state");
        else {
          edgeMap.putUnsafe(edgeKey, edgeDescription(edge)); //JSList.with(edge.codeSets()));
        }
      }
    }
    return m;
  }

  private static Object edgeProblem(Edge edge, String message) {
    return "*** problem with edge: " + message + " ***; " + JSList.with(edge.codeSets());
  }

  private static Object edgeDescription(Edge edge) {
    var cs = edge.codeSets();
    if (cs.length % 2 != 0) {
      return edgeProblem(edge, "odd number of elements");
    }
    var lst = list();
    var sb = new StringBuilder();
    for (var i = 0; i < cs.length; i += 2) {
      var a = cs[i];
      var b = cs[i + 1];
      if ((a < 0 != b < 0) || (a >= b)) {
        return edgeProblem(edge, "illegal code set");
      }
      lst.add(a);
      lst.add(b);
    }
    return lst;
  }

  public static JSMap validateNewParsing(String dfaSource) {
    JSMap result = map();
    JSMap descOld, descNew;
    {
      State.resetDebugIds();
      var json = JSMap.parseUsingOldParser(dfaSource);
      var dfa = new DFA(json);
      descOld = describe(dfa);
      result.put("1_old", descOld);
    }
    {
      State.resetDebugIds();
      var dfa = DFA.parseDfaUsingBespokeParser(dfaSource);
      descNew = describe(dfa);
      result.put("1_new", descNew);
    }
    if (!descOld.equals(descNew)) {
      result.put("*** Error ***", "descriptions differ");
    }
    return result;
  }

  private static JSMap describe(DFA dfa) {
    return DFADump(dfa);
  }
}
