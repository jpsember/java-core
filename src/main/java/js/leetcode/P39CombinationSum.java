
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import js.json.JSList;

// I thought at first that I could use linear algebra, but a) the system of linear equations
// is under constrained, so the matrix is invertible; and b) we are only interested in INTEGER
// solutions.
//
// Given the relatively small number of candidates and target, I think a set of states is the best
// approach.
//
public class P39CombinationSum {

  public static void main(String[] args) {
    new P39CombinationSum().run();
  }

  private void run() {
    x(7, 2, 3, 6, 7);
  }

  private void x(int target, int... candidates) {

    var result = combinationSum(candidates, target);
    pr(result);
  }

  private static class State {
    List<int[]> coeff = new ArrayList<>();

    void addCombo(int[] combo) {
      // If combo is already in state, do nothing
      for (var c : coeff) {
        if (Arrays.equals(c, combo))
          return;
      }
      coeff.add(combo);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (var c : coeff) {
        sb.append(' ');
        sb.append(JSList.with(c).toString());
      }
      return sb.toString();
    }
  }

  public List<List<Integer>> combinationSum(int[] candidates, int target) {
    int clen = candidates.length;

    State states[] = new State[1 + target];
    for (int i = 0; i <= target; i++)
      states[i] = new State();

    List<Integer> stack = new ArrayList<>();
    List<Integer> stack2 = new ArrayList<>();

    {
      states[0].addCombo(new int[clen]);
      stack.add(0);
    }

    Set<Integer> stackSet = new HashSet<>();

    while (!stack.isEmpty()) {
      pr(VERT_SP, "next iteration; stack size:", stack.size());
      stackSet.clear();

      for (var stateNumber : stack) {
        var s = states[stateNumber];
        pr("state #" + stateNumber + ":", s);

        for (var c : s.coeff) {
          for (int j = 0; j < clen; j++) {
            int newSum = stateNumber + candidates[j];
            if (newSum > target)
              continue;
            checkState(newSum > stateNumber);
            State s2 = states[newSum];
            int[] c2 = Arrays.copyOf(c, c.length);
            c2[j]++;
            s2.addCombo(c2);

            if (stackSet.add(newSum)) {
              pr("...pushing state to stack:", newSum);
              stack2.add(newSum);
            }
          }
        }
      }
      stack.clear();
      var tmp = stack;
      stack = stack2;
      stack2 = tmp;
    }

    var result = states[target];

    List<List<Integer>> out = new ArrayList<>();

    for (var c : result.coeff) {
      List<Integer> x = new ArrayList<>();
      for (int i = 0; i < clen; i++) {
        int j = c[i];
        for (int k = 0; k < j; k++)
          x.add(candidates[i]);
      }
      out.add(x);
    }
    return out;
  }

}