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
package js.base;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static js.base.Tools.*;

import js.base.BaseObject;
import js.data.AbstractData;
import js.json.JSList;
import js.json.JSMap;

public final class StateMachine extends BaseObject implements AbstractData {

  // ------------------------------------------------------------------
  // Construction
  // ------------------------------------------------------------------

  public StateMachine(String name) {
    mStateMachineName = name;
  }

  @Override
  protected String supplyName() {
    return mStateMachineName;
  }

  @Override
  public JSMap toJson() {
    JSMap m = super.toJson();
    int cursor = INIT_INDEX;
    for (String name : mOrderedStateNames) {
      cursor++;
      State s = stateWithName(name);
      String key = String.format("%02d %c%-" + EST_MAX_STATE_NAME_LEN + "s", //
          cursor, //
          name.equals(currentState()) ? '*' : ' ', //
          name);
      m.put(key, JSList.with(s.targetStates));
    }

    if (isBuilt()) {
      JSMap h = map();
      m.put("history", h);
      for (Action historyEntry : history())
        h.putNumbered(historyEntry.toString());
    }
    return m;
  }

  // ------------------------------------------------------------------
  // Building state machine (defining states and transitions)
  // ------------------------------------------------------------------

  /**
   * Add a state (if it doesn't already exist), and make it the current state
   */
  public StateMachine add(String stateName) {
    mCurrentState = define(stateName);
    return this;
  }

  /**
   * Add a transition from the current state to another (adding it if it doesn't
   * exist), and make the target state the current state
   */
  public StateMachine toAdd(String stateName) {
    State next = define(stateName);
    mCurrentState.targetStates.add(next.name);
    mCurrentState = next;
    return this;
  }

  /**
   * Add transitions from the current state to a list of other states (adding
   * them if they don't exist)
   */
  public StateMachine to(String... targetStateNames) {
    for (String targetStateName : targetStateNames) {
      define(targetStateName);
      mCurrentState.targetStates.add(targetStateName);
    }
    return this;
  }

  /**
   * Set start state (by default, it is the first state added). Fails if
   * requested state doesn't exist
   */
  public StateMachine withStart(String startStateName) {
    assertBuilding();
    mStartState = stateWithName(startStateName);
    return this;
  }

  /**
   * Attach a listener. Only one listener is supported; any existing one will be
   * overwritten
   */
  public StateMachine listener(Consumer<Action> listener) {
    mListener = listener;
    return this;
  }

  /**
   * Build the state machine, and make the first state that was added the
   * current state
   */
  public StateMachine build() {
    assertBuilding();
    log("build");
    checkState(!mOrderedStateNames.isEmpty(), "No states defined");
    if (mStartState == null)
      mStartState = mStateMap.get(first(mOrderedStateNames));
    Action action = new Action(null, mStartState.name, "start");
    updateHistoryWithNewState(action, mStartState);
    return this;
  }

  // ------------------------------------------------------------------
  // Operation
  // ------------------------------------------------------------------

  /**
   * Transition to current state's single target state
   */
  public boolean next() {
    return next(null);
  }

  /**
   * Transition to current state's single target state
   */
  public boolean next(String message) {
    State current = mStateMap.get(currentState());
    String nextName = current.nextState();

    return set(nextName, message);
  }

  /**
   * Transition to a target state; fails if not a valid transition
   */
  public boolean set(String newStateName, String message) {
    return auxSet(newStateName, message, false);
  }

  /**
   * Transition to a target state; fails if not a valid transition
   */
  public boolean set(String newStateName) {
    return set(newStateName, null);
  }

  /**
   * Transition to an arbitrary state, regardless of whether it is one of the
   * current state's target states
   */
  public boolean jump(String newStateName, String message) {
    return auxSet(newStateName, message, true);
  }

  /**
   * Transition to an arbitrary state, regardless of whether it is one of the
   * current state's target states
   */
  public boolean jump(String newStateName) {
    return jump(newStateName, null);
  }

  /**
   * Get most recent action
   */
  public Action lastAction() {
    assertBuilt();
    return mHistory.peekLast();
  }

  /**
   * Verify that current state is one of a set; throws exception if not
   */
  public void assertIs(String... states) {
    if (!is(states))
      badState("Unexpected state:", currentState(), "; expected:", states);
  }

  /**
   * Determine if current state has a particular name
   */
  public boolean is(String state) {
    return currentState().equals(state);
  }

  /**
   * Determine if current state's name appears in a list of names
   */
  public boolean is(String... states) {
    return nameIs(currentState(), states);
  }

  /**
   * Determine if a state name appears in a list
   */
  public static boolean nameIs(String name, String... states) {
    for (String value : states)
      if (name.equals(value))
        return true;
    return false;
  }

  /**
   * Get the name of the current state
   */
  public String currentState() {
    assertBuilt();
    return mCurrentState.name;
  }

  /**
   * Convenience method to throw an IllegalStateException
   */
  public IllegalStateException unsupported() {
    throw notSupported("Unsupported state:", toJson().prettyPrint());
  }

  /**
   * Get queue containing the most recent Actions
   */
  public ArrayDeque<Action> history() {
    assertBuilt();
    return mHistory;
  }

  private State stateWithName(String name) {
    State state = optStateWithName(name);
    if (state == null)
      throw new IllegalArgumentException("No state found with name: " + name);
    return state;
  }

  private State optStateWithName(String name) {
    return mStateMap.get(name);
  }

  private boolean auxSet(String newStateName, String message, boolean jumpMode) {
    if (is(newStateName))
      return false;

    State current = mCurrentState;
    State newState = stateWithName(newStateName);

    Action action = new Action(current.name, newState.name, message);
    if (!jumpMode && !current.targetStates.contains(newStateName))
      throw new IllegalStateException("Illegal state transition: " + action);
    updateHistoryWithNewState(action, newState);
    return true;
  }

  private void updateHistoryWithNewState(Action action, State newState) {
    if (verbose())
      log(action);
    addToHistory(action);
    mCurrentState = newState;
    if (mListener != null)
      mListener.accept(action);
  }

  private static final int MAX_HISTORY = 50;

  private Action addToHistory(Action entry) {
    while (mHistory.size() >= MAX_HISTORY)
      mHistory.removeFirst();
    mHistory.addLast(entry);
    return entry;
  }

  private State define(String stateName) {
    assertBuilding();
    log("define:", TAB(24), stateName);
    State b = optStateWithName(stateName);
    if (b == null) {
      if (stateName.length() > EST_MAX_STATE_NAME_LEN)
        alert("State name exceeds suggested max length of", EST_MAX_STATE_NAME_LEN, ":", stateName);
      b = new State(stateName);
      mStateMap.put(stateName, b);
      mOrderedStateNames.add(stateName);
    }
    return b;
  }

  private boolean isBuilt() {
    return !mHistory.isEmpty();
  }

  private void assertBuilt() {
    if (!isBuilt())
      throw new IllegalStateException("state machine isn't built yet");
  }

  private void assertBuilding() {
    if (isBuilt())
      throw new IllegalStateException("state machine already built");
  }

  private static String describeStateTransition(String sourceStateNameOrNull, String targetStateName,
      String messageOrNull) {
    StringBuilder sb = new StringBuilder();
    if (sourceStateNameOrNull != null)
      sb.append(sourceStateNameOrNull);
    tab(sb, EST_MAX_STATE_NAME_LEN);
    sb.append("--> ");
    sb.append(targetStateName);
    if (!nullOrEmpty(messageOrNull)) {
      tab(sb, EST_MAX_STATE_NAME_LEN * 2 + 5);
      sb.append("(");
      sb.append(messageOrNull);
      sb.append(")");
    }
    return sb.toString();
  }

  private static final int EST_MAX_STATE_NAME_LEN = 16;

  private static class State {

    State(String name) {
      this.name = name;
    }

    public String nextState() {
      if (targetStates.size() != 1)
        badState("State has no single target state:", name);
      return first(targetStates);
    }

    final String name;
    final List<String> targetStates = new ArrayList<>(4);
  }

  /**
   * Represents a transition event from a source state to a target state
   */
  public static class Action {

    public static final Action NONE = new Action(null, null, null);

    public Action(String sourceStateNameOrNull, String targetStateName, String transitionReason) {
      this.sourceStateName = nullToEmpty(sourceStateNameOrNull);
      this.targetStateName = targetStateName;
      this.transitionReason = nullToEmpty(transitionReason);
    }

    public String toString() {
      return describeStateTransition(sourceStateName, targetStateName, transitionReason);
    }

    public final String sourceStateName;
    public final String targetStateName;
    public final String transitionReason;
  }

  private final String mStateMachineName;
  private Consumer<Action> mListener;
  private State mStartState;

  private List<String> mOrderedStateNames = arrayList();
  private Map<String, State> mStateMap = concurrentHashMap();
  private State mCurrentState;
  private ArrayDeque<Action> mHistory = new ArrayDeque<>();
}
