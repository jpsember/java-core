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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static js.base.Tools.*;

public final class State {

  /**
   * edge label for epsilon transitions
   */
  public static final int EPSILON = -1;

  private static final List<Edge> EMPTY_EDGE_LIST = new ArrayList<>(0);

  public State(int id, boolean finalState, List<Edge> edges) {
    mId = id;
    mFinalState = finalState;
    if (edges == null || edges.isEmpty())
      mEdges = EMPTY_EDGE_LIST;
    else {
      ArrayList<Edge> ourEdges = new ArrayList<>(edges);
      ourEdges.trimToSize();
      mEdges = ourEdges;
    }
  }

  public int id() {
    return mId;
  }

  //  @Deprecated // Have constructor accept a list of edges
  //  public void setEdges(List<Edge> edgeList) {
  //    mEdges = edgeList;
  //  }

  public List<Edge> edges() {
    return mEdges;
  }

  /**
   * Add an epsilon transition to a state
   */
  @Deprecated // Move to tokn package
  public void addEps(State target) {
    edges().add(new Edge(EPSILON_RANGE, target.id()));
  }

  private static int[] EPSILON_RANGE = { EPSILON, 1 + EPSILON };

  public boolean finalState() {
    return mFinalState;
  }


  /**
   * Get range of state ids in a set; returns [lowest id, 1 + highest id]
   */
  @Deprecated // Move to tokn package
  public int[] rangeOfStateIds(Collection<State> states) {
    int max_id = -1;
    int min_id = -1;
    for (State state : states) {
      if (max_id < 0) {
        max_id = state.id();
        min_id = max_id;
      } else {
        min_id = Math.min(min_id, state.id());
        max_id = Math.max(max_id, state.id());

      }
    }
    todo("Use a code range here?");
    int[] result = new int[2];
    result[0] = min_id;
    result[1] = max_id + 1;
    return result;
  }

//  /**
//   * Duplicate the NFA reachable from this state, possibly with new ids
//   * 
//   * @param dupBaseId
//   *          lowest state id to use for duplicates
//   * @param origToDupStateMap
//   *          where to construct map of original state ids to new states
//   * @return next available state id
//   */
//  @Deprecated // Move to tokn package
//  public int duplicateNFA(int dupBaseId, Map<Integer, State> origToDupStateMap) {
//    checkArgument(origToDupStateMap.isEmpty());
//
//    Set<State> oldStates = reachableStates();
//
//    int[] res = rangeOfStateIds(oldStates);
//    int oldMinId = res[0];
//    int oldMaxId = res[1];
//
//    for (State s : oldStates) {
//      State s2 = new State((s.id() - oldMinId) + dupBaseId);
//
//      s2.finalState(s.finalState());
//      origToDupStateMap.put(s.id(), s2);
//
//    }
//    for (State s : oldStates) {
//      State s2 = origToDupStateMap.get(s.id());
//      for (Edge edge : s.edges()) {
//        s2.edges().add(new Edge(edge.codeRanges(), origToDupStateMap.get(edge.destinationStateId()).id()));
//      }
//    }
//    return (oldMaxId - oldMinId) + dupBaseId;
//  }

  /**
   * Build set of states reachable from this state
   */
  @Deprecated // Move to tokn package
  public Set<State> reachableStates() {
    Set<State> set = hashSet();
    List<State> stack = arrayList();
    push(stack, this);
    while (nonEmpty(stack)) {
      State st = pop(stack);
      set.add(st);
      for (Edge edge : st.edges()) {
        int destId = edge.destinationStateId();
        State dest = fetch(null, destId);
        if (set.add(dest))
          push(stack, dest);
      }

    }
    return set;
  }

  /**
   * <pre>
   *     # Construct the reverse of the NFA starting at this state
      # < start state of reversed NFA
      #
      def reverseNFA
  
        edgeList = []
  
        newStartStateList = []
        newFinalStateList = []
  
        newStateMap = {}
  
        stateSet = reachable_states
        stateSet.each do |s|
          u = State.new(s.id)
          newStateMap[u.id] = u
  
          if s.id == self.id
            newFinalStateList.push(u)
            u.final_state = true
          end
  
          if s.final_state
            newStartStateList.push(u)
          end
  
          s.edges.each {|lbl, dest| edgeList.push([dest.id, s.id, lbl])}
  
        end
  
        edgeList.each do |srcId, destId, lbl|
          srcState = newStateMap[srcId]
          destState = newStateMap[destId]
          srcState.addEdge(lbl, destState)
        end
  
        # Create a distinguished start node that points to each of the start nodes
        _,maxId = range_of_state_ids(stateSet)
        w = State.new(maxId)
        newStartStateList.each {|s| w.addEps(s)}
        w
      end
   * 
   * 
   * </pre>
   */

  /**
   * Given a list of states and an id (its index within that list), return the
   * state
   */
  public static State fetch(List<State> stateList, int stateId) {
    if (stateList == null)
      badState("stateList is null");
    return stateList.get(stateId);
  }

  // We probably could avoid storing the id within the state, but at the
  // cost of a lot of extra code
  //
  private final int mId;

  // TODO: can optimize by having an immutable empty list initially, if we construct fresh one before adding anything
  private final List<Edge> mEdges;

  // Is this still required?
  private final boolean mFinalState;

  public State withEdges(List<Edge> edges) {
    return new State(mId, mFinalState, edges);
  }

}