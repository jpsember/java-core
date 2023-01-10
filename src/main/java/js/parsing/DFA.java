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
package js.parsing;

import static js.base.Tools.*;

import java.util.*;

import js.data.DataUtil;
import js.json.JSList;
import js.json.JSMap;

public final class DFA {

  private static final boolean db = true && alert("db is true");
  
  public static final int UNKNOWN_TOKEN = -1;

  public DFA(String script) {
    constructFromJson(new JSMap(script));
  }

  public DFA(JSMap json) {
    constructFromJson(json);
  }

  private void constructFromJson(JSMap map) {
  if (db) pr("...................constructing DFA from json:",INDENT,map);
    State.bumpDebugIds();
    if (map.getDouble("version") < 3.0)
      throw new IllegalArgumentException("unsupported version\n" + map.prettyPrint());

    mFinalStateIndex = map.getInt("final");
    mTokenNames = map.getList("tokens");
    for (int i = 0; i < mTokenNames.size(); i++)
      mTokenNameIdMap.put(mTokenNames.getString(i), i);
    JSList stateInfo = map.getList("states");

    mStates = new State[stateInfo.size()];
    for (int i = 0; i < mStates.length; i++)
      mStates[i] = new State(i == mFinalStateIndex, null);

    List<Edge> compiledEdges = arrayList();
    int stateId;
    for (stateId = 0; stateId < mStates.length; stateId++) {
      State s = mStates[stateId];
      JSList edges = stateInfo.get(stateId);
      pr(VERT_SP,"compiling state id:",stateId,"db:",s.debugId(),"from:",edges);
      compiledEdges.clear();
      int cursorMax = edges.size();
      int cursor = 0;
      while (cursor < cursorMax) {
        JSList edgeInfo;
        Object element = edges.getUnsafe(cursor);
        if (element instanceof JSList) {
          edgeInfo = (JSList) element;
        if (db)  pr("edgeInfo is a JSList:",edgeInfo);
        } else {
          edgeInfo = list().addUnsafe(element);
          if (db)  pr("edgeInfo is a scalar:",edgeInfo);
        }
        int[] codeRangeList = buildCodeSet(edgeInfo);
        if (db) pr("code range:",codeRangeList);
        cursor++;
        
        int destStateIndex = mFinalStateIndex;
        if (cursor < cursorMax) {
          destStateIndex = edges.getInt(cursor);
          cursor++;
        }
        if (db) pr("dest state index:",destStateIndex,"of total",mStates.length);
        compiledEdges.add(new Edge(codeRangeList, getState(destStateIndex)));
      }
      if (db) todo("final state flag isn't used by DFA");
      s.setEdges(compiledEdges);
if (db)      pr("built state:",s,"final:",s.finalState(), VERT_SP);
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
    return mTokenNames.getString(tokenId);
  }

  public Integer optTokenId(String tokenName) {
    return mTokenNameIdMap.get(tokenName);
  }

  public Integer tokenId(String tokenName) {
    Integer i = optTokenId(tokenName);
    if (i == null)
      throw new IllegalArgumentException("token name not found: " + tokenName);
    return i;
  }

  /**
   * Given a space-delimeted string of token names, construct a set of their
   * indexes
   */
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("DFA\n");
    for (int i = 0; i < mTokenNames.size(); i++) {
      sb.append(" " + mTokenNames.getString(i) + "\n");
    }
    return sb.toString();
  }

  private static final int CODEMAX = 0x110000;

  /**
   * Build a code range (an array of 2n integers) from a list of integers
   */
  private static int[] buildCodeSet(JSList list) {
    List<Integer> codeRangeList = arrayList();
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
          b = CODEMAX;
      } else {
        a = ((Number) value).intValue();
        b = a + 1;
      }
      codeRangeList.add(a);
      codeRangeList.add(b);
    }
    return DataUtil.intArray(codeRangeList);
  }

  State getStartState() {
    return mStates[0];
  }

  State getState(int id) {
    return mStates[id];
  }

  State getFinalState() {
    return getState(mFinalStateIndex);
  }

  private JSList mTokenNames;
  private Map<String, Integer> mTokenNameIdMap = hashMap();
  private int mFinalStateIndex;
  private State[] mStates;
}
