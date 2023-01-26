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

  public static final int UNKNOWN_TOKEN = -1;

  public DFA(JSMap json) {
    constructFromJson(json);
  }

  private void constructFromJson(JSMap map) {
    State.bumpDebugIds();
    if (map.getDouble("version") < 3.0)
      throw badArg("unsupported version:", map.prettyPrint());

    int finalStateIndex = map.getInt("final");
    mTokenNames = map.getList("tokens").asStringArray();
    int index = INIT_INDEX;
    for (String name : mTokenNames) {
      index++;
      mTokenNameIdMap.put(name, index);
    }
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
   * UNKNOWN_TOKEN
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

  public State getStartState() {
    return mStates[0];
  }

  private State getState(int id) {
    return mStates[id];
  }

  private String[] mTokenNames;
  private Map<String, Integer> mTokenNameIdMap = hashMap();
  private State[] mStates;
}
