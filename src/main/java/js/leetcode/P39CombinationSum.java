
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import js.data.DataUtil;
import js.json.JSList;

// I thought at first that I could use linear algebra, but a) the system of linear equations
// is under constrained, so the matrix is invertible; and b) we are only interested in INTEGER
// solutions.
//
// Given the relatively small number of candidates and target, I think a set of states is the best
// approach.
//
// I realize now that this is the integer knapsack problem.
//
// First attempt: runs slower than 95% of the accepted solutions.
//
public class P39CombinationSum {

  public static void main(String[] args) {
    new P39CombinationSum().run();
  }

  private void run() {
    x(7, 2, 3, 6, 7);
    //  x(1, 2);
  }

  private void x(int target, int... candidates) {
    var result = combinationSum(candidates, target);
    pr(result);
  }

  public List<List<Integer>> combinationSum(int[] candidates, int target) {
    int numCandidates = candidates.length;

    State states[] = new State[1 + target];
    for (int i = 0; i <= target; i++)
      states[i] = new State();
    states[0].addCombination(new int[numCandidates]);

    long frontier = 1;
    while (frontier != 0) {

      pr(VERT_SP, "next iteration; stack:", DataUtil.bitString((int) (frontier >> 32)),
          DataUtil.bitString((int) frontier));
      long nextFrontier = 0;

      for (var stateNumber = 0; stateNumber < 40; stateNumber++, frontier >>= 1) {
        if ((frontier & 1) == 0)
          continue;

        var s = states[stateNumber];

        for (var c : s.combinations) {
          loop: for (int j = 0; j < numCandidates; j++) {
            // If this combination already has higher-valued values, skip this value
            for (int k = j + 1; k < numCandidates; k++)
              if (c[k] > 0)
                continue loop;
            int newSum = stateNumber + candidates[j];
            if (newSum > target)
              continue;
            State nextState = states[newSum];
            int[] newCombination = Arrays.copyOf(c, c.length);
            newCombination[j]++;
            if (nextState.addCombination(newCombination))
              nextFrontier |= 1L << newSum;
          }
        }
      }
      frontier = nextFrontier;
    }

    var targetState = states[target];

    List<List<Integer>> resultList = new ArrayList<>();

    for (var combination : targetState.combinations) {
      List<Integer> result = new ArrayList<>();
      int i = 0;
      for (var count : combination) {
        var value = candidates[i];
        for (int k = 0; k < count; k++)
          result.add(value);
        i++;
      }
      resultList.add(result);
    }
    return resultList;
  }

  private static class State {
    List<int[]> combinations = new ArrayList<>();

    boolean addCombination(int[] combo) {
      // If combination is already in the state, do nothing
      for (var c : combinations) {
        if (Arrays.equals(c, combo))
          return false;
      }
      combinations.add(combo);
      return true;
    }

    //    @Override
    //    public String toString() {
    //      StringBuilder sb = new StringBuilder();
    //      for (var c : combinations) {
    //        sb.append(' ');
    //        sb.append(JSList.with(c).toString());
    //      }
    //      return sb.toString();
    //    }
  }

}